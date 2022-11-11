#!/bin/bash -ex

mvn clean
mvn package

mkdir -p target/dependency && (cd target/dependency; jar -xf ../*.jar)

docker build -t cs598ccc/sample-app:$1 -t gcr.io/cs598-cloud-computing-capstone/sample-app:$1 .
docker push gcr.io/cs598-cloud-computing-capstone/sample-app:$1