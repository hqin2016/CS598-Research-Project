# CS598-Research-Project

## Prerequisites

The recommendation is to use the latest versions of the following

- gcloud
  - Component: gke-gcloud-auth-plugin
  - Component: kubectl
- terraform
- openjdk 19
  - Apache Maven
- Docker
- Python + pip
  - Locust

### Google Cloud Platform

It seems that with student emails we are restricted by the organization, it's recommended to just use a personal account.

After creating an account you can create a project (Or use the default created projects).

Enable the following APIs in the console by going to APIs & Services:
- Cloud Monitoring API
- Cloud Logging API
- Compute Engine API
- Cloud Storage API
- Container Registry API
- Kubernetes Engine API

*There may be a few other APIs that need enabled along the way

Then you can authenticate with the following command. Then set the project.
```
    gcloud auth login
    gcloud config set project PROJECT_ID
```

## Build 

Running from the root directory the following script will create the necessary GCP resources and build containers.

```
    unique=<unique string such as initials> version=<version for containers such as 0.1.0> ./scripts/build.sh
```

## Initialize Deployment

Running from the root directory the following script will deploy the following resources:

- Sample App
- Prometheus monitoring for the sample apps
- Application Controller
- Microservice Controllers for the sample apps

```
    topology=<one of sample|sequential|diamond|complex> version=<same as above> ./scripts/initialize.sh
```

_**NOTE**: if helm command is not available then you wil need to manually edit the files in App/manifests for the appropriate topology to point to the correct containers for deployment. Replace **danielbaiuiuc** with **gcr.io/{project-id}**. 

### Prometheus

Run the following command then copy the external IP address to a browser will allow you to access the prometheus dashboard
```
    kubectl get services -n monitoring
```

## Teardown

Running from the root directory the following script will destroy the GKE resources.

```
    unique=<same as build> ./scripts/teardown.sh
```

_**NOTE**: container resources aren't destroyed_

### Perf

By default the load test in this directory targets service-a in GKE. 

The test can be spun up easily with './scripts/load.sh' which will use your current GKE configurations and set up running with real workload.

Alternatively you can run './scripts/sample.sh' which will allow you to manually decide user/spawn rate and in advanced settings the duration of the test.
