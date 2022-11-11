#!/bin/bash -e

topology=${topology:-"sample"}
version=${version:-"0.1.0"}

project=$(gcloud config get-value project)
echo "Project ID: ${project}"

container_registry="gcr.io/${project}"

echo "[INFO] Deploying manifests to GKE"

pushd App &>/dev/null
    helm uninstall project
    if command -v helm &>/dev/null
    then
        echo "[INFO] Helm found, will initialize using helm"
        helm install project -f "project/${topology}.yaml" project \
            --set containerRegistry="$container_registry" \
            --set appVersion="$version" \
            --set controllerVersion="$version"
    elif command -v kubectl &>/dev/null
    then
        echo "[INFO] Kubectl found, will initialize using kubectl"
        kubectl apply -f "manifests/${topology}-manifest.yaml"
    else
        echo "[ERROR] No compatible commands found, exiting..."
        exit
    fi
popd &>/dev/null