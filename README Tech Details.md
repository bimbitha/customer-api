# Customer API - CI/CD, Docker, Kubernetes and New Relic Observability

## 1. Overview

This project implements and operationalizes a Spring Boot Customer API backed by PostgreSQL. The implementation progressed from application/database setup through automated CI, Docker packaging, GitHub Container Registry (GHCR), Kubernetes deployment, rolling updates, health probes, and New Relic observability.

### Current completed local architecture

```text
Developer
   |
   | git push
   v
GitHub Actions
   |
   +--> Maven tests
   +--> Maven application build
   +--> Docker image build
   `--> GHCR push
          |
          v
Docker Desktop Kubernetes (kind)
   |
   +--> Deployment (2 replicas)
   +--> RollingUpdate
   +--> startup / readiness / liveness probes
   +--> ConfigMap
   +--> Secret
   `--> Service
          |
          v
     Customer API
          |
          v
      PostgreSQL

Observability:
Kubernetes --> New Relic OTel Collector --> New Relic Cloud
Spring Boot --> New Relic Java Agent --> New Relic APM
```

## 2. Implementation status

| Area | Status | Result |
|---|---|---|
| Spring Boot Customer API | Complete | API starts and serves requests |
| PostgreSQL | Complete | Database connectivity verified |
| Flyway | Complete | Versioned migrations in repository |
| Unit/integration tests | Complete | CI test stage green |
| Docker | Complete | Application image builds/runs |
| GHCR | Complete | Image published to GitHub Container Registry |
| Local Kubernetes | Complete | kind cluster enabled and Ready |
| Deployment | Complete | 2 replicas |
| RollingUpdate | Complete | Explicit strategy configured |
| Health probes | Complete | Startup, readiness and liveness configured |
| Resource requests/limits | Complete | CPU and memory controls configured |
| New Relic Kubernetes | Complete | OTel collector sending telemetry |
| New Relic Java APM | Complete | Customer API registered in APM |
| Custom New Relic dashboard | Complete | Kubernetes + APM single-pane dashboard |
| Automatic CD from GitHub-hosted runner to local laptop Kubernetes | Not enabled | Local cluster is not remotely reachable |
| AWS EKS cloud deployment | Not completed | Requires AWS account |

## 3. Repository layout

```text
customer-api/
├── .github/
│   └── workflows/
│       └── <CI workflow>.yml
├── db/
│   └── migration/
│       ├── V2__add_customer_status.sql
│       └── V3__add_customer_type.sql
├── k8s/
│   ├── deployment.yaml
│   ├── service.yaml
│   ├── configmap.yaml
│   └── values-newrelic.yaml
├── newrelic/
│   ├── newrelic.jar
│   └── newrelic.yml
├── scripts/
│   └── deploy.sh
├── src/
├── Dockerfile
├── pom.xml
└── README.md
```

## 4. Application and database

### PostgreSQL

The application uses PostgreSQL database `customer_db` on port 5432.

Inside Kubernetes, the application connects to the Windows host PostgreSQL through:

```text
jdbc:postgresql://host.docker.internal:5432/customer_db
```

`localhost` from inside a container/Pod refers to the container/Pod itself, which is why `host.docker.internal` is used for this local development topology.

### Flyway migrations

Schema changes are version controlled under:

```text
db/migration/
├── V2__add_customer_status.sql
└── V3__add_customer_type.sql
```

This keeps database evolution repeatable and tied to source control.

## 5. GitHub Actions CI

The pipeline currently performs:

```text
Checkout source
      |
Java 21
      |
Maven wrapper executable
      |
Run tests
      |
Build application
      |
Login to GHCR
      |
Build Docker image
      |
Push Docker image
```

The pipeline also creates a `latest` image and a commit-SHA-tagged image.

### Important release principle

`latest` is convenient, but an immutable SHA tag is better for traceability:

```text
ghcr.io/bimbitha/customer-api:abc123...
```

The intended future deployment pattern is:

```text
GITHUB_SHA
   |
   +--> GHCR image:<SHA>
   |
   `--> kubectl set image deployment/customer-api customer-api=...:<SHA>
```

The repository contains `scripts/deploy.sh` with this deployment logic.

## 6. Docker

The Docker image uses Eclipse Temurin Java 21.

Final Dockerfile pattern:

```dockerfile
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY target/customer-api-0.0.1-SNAPSHOT.jar /app/app.jar

COPY newrelic/newrelic.jar /app/newrelic/newrelic.jar
COPY newrelic/newrelic.yml /app/newrelic/newrelic.yml

LABEL org.opencontainers.image.source="https://github.com/bimbitha/customer-api"

EXPOSE 8080

ENTRYPOINT ["java", "-javaagent:/app/newrelic/newrelic.jar", "-jar", "/app/app.jar"]
```

A Docker build failure occurred after introducing New Relic because the `ENTRYPOINT` was initially written in a multiline form that was not parsed correctly. It was corrected to the JSON-array form on one line.

## 7. GHCR

Images are published to:

```text
ghcr.io/bimbitha/customer-api
```

The image is tagged with:

```text
latest
<Git commit SHA>
```

This creates both convenience and release traceability.

## 8. Local Kubernetes

Docker Desktop Kubernetes was enabled using a single-node `kind` cluster.

Verification:

```powershell
kubectl get nodes
```

Expected state:

```text
desktop-control-plane   Ready
```

### Deployment

The application Deployment runs two replicas:

```yaml
replicas: 2
```

The strategy is explicitly configured:

```yaml
strategy:
  type: RollingUpdate
  rollingUpdate:
    maxUnavailable: 0
    maxSurge: 1
```

Meaning:

- Kubernetes can create one temporary extra Pod during rollout.
- Existing availability is protected while the new Pod becomes ready.
- Old Pods are removed after new Pods become ready.

### Health probes

The final Deployment uses three distinct probes:

```text
startupProbe
  -> Has the application finished starting?

readinessProbe
  -> Can this Pod receive traffic?

livenessProbe
  -> Is the application still alive?
```

The startup probe was added after observing that Spring Boot startup took approximately 55 seconds.

Final startup probe:

```yaml
startupProbe:
  httpGet:
    path: /actuator/health
    port: 8080
  periodSeconds: 10
  timeoutSeconds: 3
  failureThreshold: 12
```

This provides roughly 120 seconds of startup allowance.

### Resource limits

The application Pod has:

```yaml
resources:
  requests:
    cpu: "250m"
    memory: "256Mi"
  limits:
    cpu: "500m"
    memory: "512Mi"
```

## 9. Kubernetes Service

The Customer API is exposed through:

```text
customer-api-service
```

using a NodePort.

For local testing, port forwarding was used:

```powershell
kubectl port-forward service/customer-api-service 8082:8080
```

Postman then called:

```text
GET http://localhost:8082/api/customers
```

The API was verified successfully through the Kubernetes Service.

## 10. Kubernetes troubleshooting performed

### Issue 1 - Deployment rollout stalled

The new Pod repeatedly failed liveness/readiness because the Spring Boot application needed more time to start.

Kubernetes reported connection refused on:

```text
/actuator/health/liveness
/actuator/health/readiness
```

The application log showed:

```text
Tomcat started on port 8080
Started CustomerApiApplication in ~54.6 seconds
```

The fix was to add the startup probe.

### Issue 2 - New Relic collector returned HTTP 403

The New Relic Kubernetes collector initially exported to:

```text
https://otlp.nr-data.net
```

The New Relic account was in the EU region.

The endpoint was corrected to:

```text
https://otlp.eu01.nr-data.net
```

After this, Kubernetes metrics started arriving in New Relic.

### Issue 3 - New Relic Java agent initially rejected the key

The Java agent was loaded, but the agent log showed:

```text
Invalid license key, the agent is no longer reporting information.
```

The Kubernetes application Secret was corrected with a valid New Relic Ingest - License key.

The agent was then configured to report to the EU collector.

## 11. New Relic Kubernetes observability

Helm was installed and the New Relic Kubernetes OpenTelemetry collector was deployed:

```text
newrelic/nr-k8s-otel-collector
```

Namespace:

```text
newrelic
```

The active components were verified as healthy:

```text
nr-k8s-otel-collector-daemonset              Running
nr-k8s-otel-collector-deployment             Running
nr-k8s-otel-collector-kube-state-metrics    Running
```

### Metric discovery

A generic Kubernetes sample query returned no matching records, so actual dimensional `Metric` telemetry was discovered.

Confirmed metric examples include:

```text
k8s.pod.cpu_limit_utilization
k8s.pod.cpu_request_utilization
k8s.node.cpu.usage
container.cpu.usage

k8s.pod.memory_request_utilization
k8s.pod.memory_request_limit_ratio
k8s.pod.memory.working_set
container.memory.usage
node.memory.usage.percentage

kube_pod_status_phase
kube_deployment_status_observed_generation
```

The cluster resource attribute is:

```text
k8s.cluster.name = 'customer-api-local'
```

## 12. New Relic Java APM

The New Relic Java agent is installed in the application image.

The running Pod contains:

```text
/app/newrelic/newrelic.jar
/app/newrelic/newrelic.yml
```

The Java process was verified as:

```text
java -javaagent:/app/newrelic/newrelic.jar -jar /app/app.jar
```

The final agent log confirmed:

```text
New Relic Agent v9.4.0 has started
Agent ... /Customer API connected to collector.eu01.nr-data.net:443
Reporting to the EU New Relic account
```

### APM results

The Customer API application appeared under:

```text
New Relic -> APM & Services -> Customer API
```

Observed telemetry included:

```text
Throughput
Response time
Error rate
Transactions
```

Example observed transactions:

```text
api/customers (POST)   ~773 ms average
api/customers (GET)    ~309 ms average
```

The observed error rate was 0% in the captured APM window.

## 13. New Relic dashboard

The custom dashboard is:

```text
Customer API - Kubernetes & Application Health
```

The dashboard combines:

### Kubernetes

- Pod count
- Kubernetes metric volume
- Pod CPU
- CPU request utilization
- Node CPU
- Container memory
- Pod memory request utilization
- Node memory
- Pod status
- CPU request/limit ratio
- Memory request/limit ratio
- Deployment telemetry
- Kubernetes events

### Application APM

- API throughput
- Average response time
- P95 response time
- Error rate
- Transactions by endpoint
- Errors by endpoint
- Slowest transactions
- Request volume by endpoint

## 14. Security

Secrets are not committed into source files.

PostgreSQL and New Relic credentials are supplied through Kubernetes Secrets.

The New Relic license key should not be stored in:

```text
newrelic.yml
Dockerfile
README.md
dashboard JSON
GitHub source files
```

The application receives the license through:

```text
NEW_RELIC_LICENSE_KEY
```

The New Relic dashboard JSON should remain private because it contains the New Relic account identifier and operational configuration.

## 15. Why full automatic CD is not yet enabled

The current Kubernetes cluster runs locally:

```text
Docker Desktop -> kind -> local Kubernetes
```

A standard GitHub-hosted Actions runner is outside the laptop and cannot directly reach the local Kubernetes API.

Therefore:

```text
git push -> tests -> Docker -> GHCR
```

is automated,

while:

```text
GHCR -> local laptop Kubernetes
```

is not automatically performed by GitHub-hosted Actions.

### Future cloud architecture

When an AWS account is available:

```text
GitHub Actions
      |
      | OIDC
      v
AWS IAM role
      |
      v
EKS
      |
      v
kubectl set image using GITHUB_SHA
      |
      v
RollingUpdate
      |
      v
New Relic
```

This avoids long-lived AWS credentials in GitHub Secrets.

## 16. Useful commands

### Git

```powershell
git status
git log -1 --oneline
git add .
git commit -m "<message>"
git push origin master
```

### Kubernetes

```powershell
kubectl get nodes
kubectl get pods
kubectl get deployment customer-api
kubectl get rs -l app=customer-api
kubectl get service customer-api-service
kubectl rollout status deployment/customer-api
kubectl rollout history deployment/customer-api
kubectl rollout undo deployment/customer-api
```

### Pod diagnostics

```powershell
kubectl describe pod <pod-name>
kubectl logs <pod-name>
kubectl logs <pod-name> --previous
```

### New Relic agent diagnostics

```powershell
kubectl exec <pod-name> -- ls -lh /app/newrelic
kubectl exec <pod-name> -- sh -c "tr '\0' ' ' < /proc/1/cmdline"
kubectl exec <pod-name> -- sh -c "tail -50 /app/newrelic/logs/newrelic_agent.log"
```

### Port forwarding

```powershell
kubectl port-forward service/customer-api-service 8082:8080
```

## 17. Final verification checklist

- [x] Application runs with Java 21.
- [x] PostgreSQL connectivity works.
- [x] Flyway migrations are versioned.
- [x] Tests pass in CI.
- [x] Docker image builds.
- [x] GHCR image is published.
- [x] Kubernetes cluster is Ready.
- [x] Deployment has 2 replicas.
- [x] RollingUpdate is explicit.
- [x] Startup probe is configured.
- [x] Readiness probe is configured.
- [x] Liveness probe is configured.
- [x] CPU/memory requests and limits are configured.
- [x] Customer API Service is reachable.
- [x] New Relic Kubernetes collector is healthy.
- [x] Kubernetes metrics reach New Relic.
- [x] New Relic Java agent is loaded.
- [x] Customer API appears in New Relic APM.
- [x] API throughput/response-time/error telemetry is visible.
- [x] Custom New Relic dashboard is saved.

## 18. Closing the repository work

After copying this README into the repository and placing the Word report in the project root:

```powershell
git status
git add README.md Customer_API_Implementation_Report.docx
git commit -m "Document Customer API CI Kubernetes and New Relic observability"
git push origin master
```

The local implementation is then documented and committed as a complete engineering work package.

