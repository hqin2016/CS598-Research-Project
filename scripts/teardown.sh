#!/bin/bash -e

unique_string=${unique:?}

echo "[INFO] Initializing teardown process for IAC..."

project=$(gcloud config get-value project)
echo "[INFO] Project ID: ${project}"

pushd IAC &>/dev/null
    bucket=$(unique=$unique_string ./scripts/setup_iac.sh)
    bucket=$bucket ./scripts/destroy.sh
popd &>/dev/null

echo "[INFO] GCP Resource destroyed"