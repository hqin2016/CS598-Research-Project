#!/bin/bash -e

[[ -n "$DEBUG_SCRIPT" ]] && set -x

export PLAN_DESTROY=1
./scripts/plan.sh

terraform -chdir=terraform apply terraform.tfplan