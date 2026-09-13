#!/bin/bash

set -e

IMAGE="ghcr.io/${GITHUB_REPOSITORY}:${GITHUB_SHA}"

echo "Deploying image: ${IMAGE}"

kubectl set image deployment/customer-api \
  customer-api="${IMAGE}"

echo "Waiting for rollout..."

kubectl rollout status deployment/customer-api --timeout=180s

echo "Deployment successful"

kubectl get deployment customer-api
kubectl get pods