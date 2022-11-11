#!/bin/bash -e

bucket=${bucket:?}

[[ -n "$DEBUG_SCRIPT" ]] && set -x

terraform -chdir=terraform init -backend-config="bucket=${bucket}" 

flags=""
if [[ -n "$PLAN_DESTROY" ]]; then
    flags="-destroy ${flags}"
fi

terraform -chdir=terraform plan -out=terraform.tfplan $flags

terraform -chdir=terraform show -no-color terraform.tfplan > terraform/terraform.tfplan.txt