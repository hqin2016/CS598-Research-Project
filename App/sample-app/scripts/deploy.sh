#!/bin/bash -ex

kubectl apply -f ./devops/deployment.yaml
kubectl apply -f ./devops/hpa.yaml
kubectl apply -f ./devops/service.yaml
kubectl apply -f ./devops/ingress.yaml