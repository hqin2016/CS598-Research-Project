#!/bin/bash -e

unique_string=${unique:?}
version=${version:?}

echo "[INFO] Initializing building process for IAC and containers..."

project=$(gcloud config get-value project)
echo "[INFO] Project ID: ${project}"

if ! command -v gcloud &>/dev/null
then
    echo "[ERROR] Gcloud command not found, required for this project"
    exit
elif ! command -v terraform &>/dev/null
then
    echo "[WARN] Terraform command not found, required to instanciate GCP resources"
    echo "[INFO] Alternative: use gcloud CLI commands"
    exit
elif ! command -v docker &>/dev/null
then
    echo "[WARN] Docker command not found, required to build container"
    exit
fi

pushd IAC &>/dev/null
    bucket=$(unique=$unique_string ./scripts/setup_iac.sh)
    bucket=$bucket ./scripts/apply.sh
    gcloud container clusters get-credentials gke-cluster-centralus-1 --region us-central1-a
popd &>/dev/null

pushd App &>/dev/null
    version=$version ./scripts/build.sh
popd &>/dev/null

echo "[INFO] GCP Resource and Containers created"