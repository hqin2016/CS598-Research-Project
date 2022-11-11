#!/bin/bash -e

# fix error for failed to call webhook due to x509
ns=default
CA=$(kubectl -n $ns get secret ingress-nginx-admission -ojsonpath='{.data.ca}')
kubectl patch validatingwebhookconfigurations ingress-nginx-admission -n $ns --type='json' -p='[{"op": "add", "path": "/webhooks/0/clientConfig/caBundle", "value":"'$CA'"}]'