#!/bin/bash -e

cluster_ip=$(kubectl get service ingress-nginx-controller --output jsonpath='{.status.loadBalancer.ingress[0].ip}' | tr -d '"')
host="http://${cluster_ip}.nip.io"

echo "Starting warmup on port 8090"
locust -f locustfile_warmup.py --web-port 8090 --host $host