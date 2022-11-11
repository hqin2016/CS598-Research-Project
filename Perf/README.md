# CS598 Cloud Computing Capstone Research Project

This is the repository containing the load testing portion of this project

## Prerequisites

- python (v3.10.8)
- pip3 (v22.2.2)
- locust (v2.12.2)

## Run

### Sample

Run the following script to start a simple perf program that allows manual adjustment of user count, spawn rate, and test duration.

```
    ./scripts/sample.sh
```

### Workload

Run the following script to start the real world workload. The projected run time is 160 minutes.

_If there is a need to test with a shorter time frame, modify second_conversion inside locustfile.py (i.e. 10 = 1/6 the time)_

```
    ./scripts/load.sh
```