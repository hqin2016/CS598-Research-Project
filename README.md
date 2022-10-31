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

## Build and Run

### IAC

Follow these steps to deploy resources to GCP:
1. Go to IAC directory
2. Go to scripts/setup_iac.sh and change the db-storage-centralus to something prepended with your initials instead
   1. Update terraform/backend.tf to use the same storage bucket
3. Run './scripts/setup_iac.sh' to create backend storage for resources
4. Run './scripts/apply.sh' which will create the resources
   1. If your CLIs and environment is set up correctly, this will take about 10 minutes
5. To destroy the resources run './scripts/destroy.sh' (Used to clean up resources if needed)

After the resources are created, run the following to get the kubernetes credentials:
```
    gcloud container clusters get-credentials gke-cluster-centralus-001 --region us-central1-a
```

### App

This directory holds the sample application deployed in GKE to represent services as well as the controllers to scale the deployments.

#### Sample App

To run the sample app locally run the following in the sample-app directory:
```
    mvn package
    java -jar target/sample-app-0.0.1-SNAPSHOT.jar
```
The endpoint will then be available to call at http://localhost:8080. For more information on additional configuration, go to the README inside sample-app directory. If running the sample-app locally and wanting to run perf test against it, the perf test will need to be updated by removing 'service-a' from the GET url.

To deploy the application on GKE, first ensure there is an image in Google Container Registry. If you followed the README then the first image is of the form gcr.io/<repo-name\>/sample-app:0.0.1. Update the devops/deployment.yaml image property to be that value.

Then run the following scripts inside the sample-app directory:
```
    ./scripts/fix_errors.sh // sometimes the services connection is weird, should only need to do this once
    ./scripts/deploy.sh <optional topology argument of sequential|diamond|complex, default is sequential>
```

The following commands will help you validate your kubernetes deployments:
```
    kubectl get pods //lists the deployed pods
    kubectl get deployments //show all deployments
    kubectl get hpa //shows the horizontal pod scalers
    kubectl get services //shows the services, including the ingress
    kubectl get ingress //shows the ingress for each deployment
    kubectl describe pod <name of pod> //get additional information of the pod, can be used with any of the other types
```

#### Microservice Controller

This controller is currently only runnable in local. Follow the README in application-controller directory.

This controller expects the application controller to be on port 3000.

Build the application with 'mvn package'

Running running the following commands inside App directory will start the controllers:
```
    ./scripts/build_controllers.sh //run both application and microservice
```

#### Application Controller

This controller is currently only runnable in local. Follow the README in application-controller directory.

This controller expects the microservice controllers to be from port range 3001-3007.

Build the application with 'mvn package'

Running running the following commands inside App directory will start the controllers:
```
    ./scripts/build_controllers.sh //run both application and microservice
```

### Perf

By default the load test in this directory targets service-a in GKE. 

The test can be spun up easily with './scripts/load/sh' which will use your current GKE configurations and set up running with real workload.

Alternatively you can run './scripts/warmup/sh' which will allow you to manually decide user/spawn rate and in advanced settings the duration of the test.
