# Microservice Controller 

## Prerequisites

Need to have a service account created through IAM console. Update _GOOGLE\_APPLICATION\_CREDENTIALS_ environment variable to the path containing the credential json generated when creating a key for the service account. 

## Build

```
    mvn package
```

## Run

```
    java -jar target/microservice-controller-0.0.1-SNAPSHOT.jar <service name such as service-a|service-b|..> --server.port=3001
```