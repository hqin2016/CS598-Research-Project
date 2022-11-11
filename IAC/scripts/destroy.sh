#!/bin/bash -e

bucket=${bucket:?}

[[ -n "$DEBUG_SCRIPT" ]] && set -x

export PLAN_DESTROY=1

echo "[INFO] Generating IAC plan for resource destroy"
bucket=$bucket ./scripts/plan.sh

echo "[INFO] Executing resource destroy"
terraform -chdir=terraform apply terraform.tfplan