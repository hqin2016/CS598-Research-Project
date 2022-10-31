#!/bin/bash -e

topology=${1:-"sequential"}

> pid.txt

pushd application-controller >/dev/null

# Run target jar
# Send stdout to file
# Send error to stdout
# Run in background
java -jar target/application-controller-0.0.1-SNAPSHOT.jar $topology --server.port=3000 > log.txt 2>&1 &

# Echo PID of last command
echo $! >> ../pid.txt

popd >/dev/null

pushd microservice-controller >/dev/null

function deployMicroserviceController() {
    local service_name=$1
    local service_port=$2

    java -jar target/microservice-controller-0.0.1-SNAPSHOT.jar $service_name --server.port=$service_port > "$service_name-log.txt" 2>&1 &
    echo $! >> ../pid.txt
}

deployMicroserviceController service-a 3001
deployMicroserviceController service-b 3002
deployMicroserviceController service-c 3003
deployMicroserviceController service-d 3004
deployMicroserviceController service-e 3005
deployMicroserviceController service-f 3006
deployMicroserviceController service-g 3007

popd >/dev/null