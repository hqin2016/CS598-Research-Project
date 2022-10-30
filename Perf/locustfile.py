import csv
import logging
import random
from locust import HttpUser, task, LoadTestShape, between, events

logging.basicConfig(filename='run.txt', filemode='w', format='[%(asctime)s]%(message)s', level=logging.INFO, datefmt='%Y-%m-%d %H:%M:%S')

class HelloWorldUser(HttpUser):
    wait_time = between(0.5, 1.5)
    @task
    def hello_world(self):
        endpoint = "/service-a?n={}"
        with self.client.get(endpoint.format(random.randint(0, 100)), name="/", catch_response=True) as response:
            logging.info(f"caller:{response.elapsed}|{response.text}")

class SimpleRampShape(LoadTestShape):
    end_step = 161
    spawn_rate = 100 #High enough to be instant spawn
    second_conversion = 5 #How many seconds per step
    scaling = 1 #How to scale the amplitude

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