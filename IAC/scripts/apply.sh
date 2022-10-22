#!/bin/bash -e

[[ -n "$DEBUG_SCRIPT" ]] && set -x

./scripts/plan.sh

terraform -chdir=terraform apply terraform.tfplan