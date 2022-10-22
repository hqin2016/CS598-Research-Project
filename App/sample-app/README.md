# Sample Application

## Build

```
    mvn package
```

## Run

```
    java -jar target/sample-app-0.0.1-SNAPSHOT.jar
```

## Deployment

Split the jar file to improve performance with caching
```
    mkdir -p target/dependency && (cd target/dependency; jar -xf ../*.jar)
```

Build the docker image as both a version and latest
```
    docker build -t cs598ccc/sample-app:0.0.1 -t cs598ccc/sample-app:latest .
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

### GET /hello
