#!/bin/bash -e

kill $(cat pid.txt)

kubectl scale deployment service-a-deployment --replicas=1
kubectl scale deployment service-b-deployment --replicas=1
kubectl scale deployment service-c-deployment --replicas=1
kubectl scale deployment service-d-deployment --replicas=1
kubectl scale deployment service-e-deployment --replicas=1
kubectl scale deployment service-f-deployment --replicas=1
kubectl scale deployment service-g-deployment --replicas=1