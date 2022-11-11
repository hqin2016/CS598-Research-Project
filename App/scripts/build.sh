#!/bin/bash -e

version=${version:-"0.1.0"}

project=$(gcloud config get-value project)
container_registry="gcr.io/${project}"

function create_image() {
    local step=$1
    local image=$2

    log_file="${step}-log.txt"

    mvn clean -q
    mvn -l $log_file package -q
    mkdir -p target/dependency && (cd target/dependency; jar -xf ../*.jar)

    docker build -q -t $image .
    # docker push -q $image
}

# Installing ARIMA package to maven
mvn install:install-file -q -Dfile=timeseries-forecast/timeseries-forecast-1.1.1.jar -DpomFile=timeseries-forecast/pom.xml

pushd sample-app &>/dev/null
echo "[INFO] Building container for sample-app"
    image="${container_registry}/sample-app:${version}"
    create_image "sample-app" $image
popd &>/dev/null

# pushd application-controller &>/dev/null
    echo "[INFO] Building container for application-controller"
#     image="${container_registry}/application-controller:${version}"
#     create_image "application-controller" $image
# popd &>/dev/null

# pushd microservice-controller &>/dev/null
    echo "[INFO] Building container for microservice-controller"
#     image="${container_registry}/microservice-controller:${version}"
#     create_image "microservice-controller" $image
# popd &>/dev/null