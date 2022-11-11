import csv
import logging
import random
from locust import HttpUser, task, LoadTestShape, between, events

class HelloWorldUser(HttpUser):
    wait_time = between(1, 2)
    @task
    def hello_world(self):
        endpoint = "/?n={}"
        self.client.get(endpoint.format(random.randint(0, 100)), name="/")