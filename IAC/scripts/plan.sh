#!/bin/bash -e

[[ -n "$DEBUG_SCRIPT" ]] && set -x

git_branch=$(git branch --show-current)

terraform -chdir=terraform init -backend-config="prefix=terraform/${git_branch}"

flags=""
if [[ -n "$PLAN_DESTROY" ]]; then
    flags="-destroy ${flags}"
fi

terraform -chdir=terraform plan -out=terraform.tfplan $flags

terraform -chdir=terraform show -no-color terraform.tfplan > terraform/terraform.tfplan.txt