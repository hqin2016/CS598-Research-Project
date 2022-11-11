#!/bin/bash -ex

mvn clean
mvn package

mkdir -p target/dependency && (cd target/dependency; jar -xf ../*.jar)

docker build -t cs598ccc/application-controller:$1 -t gcr.io/cs598-cloud-computing-capstone/application-controller:$1 .
docker push gcr.io/cs598-cloud-computing-capstone/application-controller:$1