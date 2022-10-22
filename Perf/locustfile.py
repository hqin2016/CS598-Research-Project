import csv
import logging
from locust import HttpUser, task, LoadTestShape, between

class HelloWorldUser(HttpUser):
    wait_time = between(5, 10)
    @task
    def hello_world(self):
        with self.client.get("/hello", catch_response=True) as response:
            if response.text != "Hello World!":
                response.failure("Wrong response")

class SimpleRampShape(LoadTestShape):
    end_step = 161
    spawn_rate = 100 #High enough to be instant spawn
    second_conversion = 30 #How many seconds per step
    scaling = 0.25 #How to scale the amplitude

    def __init__(self):
        with open('workload_base.csv', mode='r') as file:
            reader = csv.reader(file)
            self.workload_dict = {rows[0]:int(int(rows[1]) * self.scaling) for rows in reader}

    def tick(self):
        run_time = self.get_run_time()
        current_step = int(run_time / self.second_conversion)
        if current_step < self.end_step:
            return (self.workload_dict[str(current_step)], self.spawn_rate)

        return None