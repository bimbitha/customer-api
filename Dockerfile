FROM eclipse-temurin:21-jre

WORKDIR /app

COPY target/customer-api-0.0.1-SNAPSHOT.jar app.jar

COPY newrelic/newrelic.jar /app/newrelic/newrelic.jar
COPY newrelic/newrelic.yml /app/newrelic/newrelic.yml

LABEL org.opencontainers.image.source="https://github.com/bimbitha/customer-api"

EXPOSE 8080

ENTRYPOINT [
  "java",
  "-javaagent:/app/newrelic/newrelic.jar",
  "-jar",
  "/app/app.jar"
]