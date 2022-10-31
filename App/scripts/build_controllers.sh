#!/bin/bash -e

pushd application-controller >/dev/null
mvn package >/dev/null
popd >/dev/null

pushd microservice-controller >/dev/null
mvn package >/dev/null
popd >/dev/null