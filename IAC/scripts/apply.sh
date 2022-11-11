#!/bin/bash -e

bucket=${bucket:?}

[[ -n "$DEBUG_SCRIPT" ]] && set -x

echo "[INFO] Generating IAC plan for resource creation"
bucket=$bucket ./scripts/plan.sh &>/dev/null

echo "[INFO] Executing resource creation"
terraform -chdir=terraform apply terraform.tfplan