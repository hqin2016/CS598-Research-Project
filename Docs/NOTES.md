# Notes on run results

## Resource request and limit

The cluster has a node pool of 5 nodes, each on n1-standard-2 instance type which has 2 vCPUs and 7.5 GiB Memory.

Each deployment request 200m of CPU and 200Mi of Memory.

Each deployment has a limit of 400m of CPU and 400Mi of Memory.

## Running at 1/12th timestep

Running at 1/12th timestep means that every data point is in 5 second intervals instead of 1 minute intervals.

### Default HPA

*It should be noted that the cluster is cold started with each test so the first few minutes of run time greatly skews metrics. 
*It should be noted that there is a maximum of 5 replicas.

**Sequential with 50% Utilization Scaling**
`Average CPU Utilization`: 50%
`Average Response Time`: 502ms
`Average Replica Count`: 4.125

**Sequential 80% Utilization Scaling**
`Average CPU Utilization`: 48%
`Average Response Time`: 469ms
`Average Replica Count`: 4.04

**Diamond with 50% Utilization Scaling**
`Average CPU Utilization`: 25%
`Average Response Time`: 212ms
`Average Replica Count`: 2.606

**Diamond with 80% Utilization Scaling**
`Average CPU Utilization`: 25%
`Average Response Time`: 220ms
`Average Replica Count`: 2.105

**Complex with 50% Utilization Scaling**
`Average CPU Utilization`: 32%
`Average Response Time`: 280ms
`Average Replica Count`: 3.812

**Complex with 80% Utilization Scaling**
`Average CPU Utilization`: 31%
`Average Response Time`: 286ms
`Average Replica Count`: 2.828