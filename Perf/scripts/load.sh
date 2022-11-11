#!/bin/bash -e

cluster_ip=$(kubectl get service application-external --output jsonpath='{.status.loadBalancer.ingress[0].ip}' | tr -d '"')
host="http://${cluster_ip}.nip.io"

echo "Starting load test on port 8091"
locust --skip-log-setup --web-port 8091 --host $host