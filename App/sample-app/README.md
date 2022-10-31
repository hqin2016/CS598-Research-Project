# Sample Application

## Build

```
    mvn package
```

## Run

```
    java -jar target/sample-app-0.0.1-SNAPSHOT.jar
```

## Deployment to GKE

Split the jar file to improve performance with caching
```
    mkdir -p target/dependency && (cd target/dependency; jar -xf ../*.jar)
```

Build the docker image as both a version and latest
```
    docker build -t cs598ccc/sample-app:0.0.1 .
```

Testing the docker image locally
```
    docker run -p 8080:8080 cs598ccc/sample-app
```

Re-tag the docker image then push to Docker Hub
```
    docker image tag cs598ccc/sample-app <account_name>/<repo_name>:sample-app-0.0.1
    docker push <account_name>/<repo_name>:sample-app-0.0.1
```

## REST Endpoints

### GET / {n: number}
Runs the factorial of 10,000 + n * 10
Responds with OK

### GET /health
Responds with OK

### GET /status
Response with average response time of child service.

## Environment Variables

The following environment variables are used to control aspects of the application:
- FACTORIAL_BASE: instead of 10k + n * 10, it's FACTORIAL_BASE + n*10
- SERVICE_TYPE: either LEAF or NODE, defaults to LEAF, leaf will not call dependencies while node will randomly choose one dependency to call
- HOST_ENDPOINT: defaults to localhost:8080, this is used when in GKE to know how to talk to other services
- DEPENDENCIES: defaults to health which when in local will call the service's own health endpoint