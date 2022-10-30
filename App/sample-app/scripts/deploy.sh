#!/bin/bash -e

topology=${1:-"sequential"}

cluster_ip=$(kubectl get service ingress-nginx-controller --output jsonpath='{.status.loadBalancer.ingress[0].ip}' | tr -d '"')

function deployApp() {
    rm -rf devops-tmp

    local service_name=$1
    local service_type=$2
    local dependencies=${3:-"NA"}

    cp -r devops/ devops-tmp
    for file in devops-tmp/*.yaml; do
        sed -i "s/\[SERVICE_NAME\]/$service_name/g" $file
        sed -i "s/\[SERVICE_TYPE\]/$service_type/g" $file
        sed -i "s/\[DEPENDENCIES\]/$dependencies/g" $file
        sed -i "s/\[CLUSTER_IP\]/$cluster_ip/g" $file
    done

    kubectl apply -f ./devops-tmp/deployment.yaml
    kubectl apply -f ./devops-tmp/hpa.yaml
    kubectl apply -f ./devops-tmp/service.yaml
    kubectl apply -f ./devops-tmp/ingress.yaml

    rm -rf devops-tmp
}


case $topology in
    complex)
        echo "Deploying complex topology"
        deployApp "service-a" "NODE" "service-b service-c service-d"
        deployApp "service-b" "NODE" "service-e"
        deployApp "service-c" "NODE" "service-e service-d"
        deployApp "service-d" "NODE" "service-f"
        deployApp "service-e" "LEAF"
        deployApp "service-f" "NODE" "service-g"
        deployApp "service-g" "LEAF" 
    ;;

    diamond)
        echo "Deploying diamond topology"
        deployApp "service-a" "NODE" "service-b service-c service-d service-e service-f"
        deployApp "service-b" "NODE" "service-g"
        deployApp "service-c" "NODE" "service-g"
        deployApp "service-d" "NODE" "service-g"
        deployApp "service-e" "NODE" "service-g"
        deployApp "service-f" "NODE" "service-g"
        deployApp "service-g" "LEAF" 
    ;;

    *)
        echo "Deploying sequencial topology"
        deployApp "service-a" "NODE" "service-b"
        deployApp "service-b" "NODE" "service-c"
        deployApp "service-c" "NODE" "service-d"
        deployApp "service-d" "NODE" "service-e"
        deployApp "service-e" "NODE" "service-f"
        deployApp "service-f" "NODE" "service-g"
        deployApp "service-g" "LEAF" 
    ;;
esac