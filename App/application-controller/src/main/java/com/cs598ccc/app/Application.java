package com.cs598ccc.app;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.cs598ccc.app.models.DeploymentScaleModel;

@SpringBootApplication
@RestController
@EnableScheduling
public class Application {
    private static Map<String, List<String>> deploymentGraph;
    private Map<String, String> deploymentToPort = Map.ofEntries(
            Map.entry("service-a-deployment", "3001"),
            Map.entry("service-b-deployment", "3002"),
            Map.entry("service-c-deployment", "3003"),
            Map.entry("service-d-deployment", "3004"),
            Map.entry("service-e-deployment", "3005"),
            Map.entry("service-f-deployment", "3006"),
            Map.entry("service-g-deployment", "3007"));

    private static HttpClient httpClient = HttpClient.newHttpClient();
    private Queue<DeploymentScaleModel> actionQueue = new LinkedList<>();

    public static void main(String[] args) {
        switch (args[0]) {
            case "complex":
                deploymentGraph = Map.ofEntries(
                        Map.entry("service-a-deployment",
                                Arrays.asList("service-b-deployment", "service-c-deployment", "service-d-deployment")),
                        Map.entry("service-b-deployment", Arrays.asList("service-e-deployment")),
                        Map.entry("service-c-deployment",
                                Arrays.asList("service-e-deployment", "service-d-deployment")),
                        Map.entry("service-d-deployment", Arrays.asList("service-f-deployment")),
                        Map.entry("service-e-deployment", Arrays.asList()),
                        Map.entry("service-f-deployment", Arrays.asList("service-g-deployment")),
                        Map.entry("service-g-deployment", Arrays.asList()));
                break;
            case "diamond":
                deploymentGraph = Map.ofEntries(
                        Map.entry("service-a-deployment",
                                Arrays.asList("service-b-deployment", "service-c-deployment", "service-d-deployment",
                                        "service-e-deployment",
                                        "service-f-deployment")),
                        Map.entry("service-b-deployment", Arrays.asList("service-g-deployment")),
                        Map.entry("service-c-deployment", Arrays.asList("service-g-deployment")),
                        Map.entry("service-d-deployment", Arrays.asList("service-g-deployment")),
                        Map.entry("service-e-deployment", Arrays.asList("service-g-deployment")),
                        Map.entry("service-f-deployment", Arrays.asList("service-g-deployment")),
                        Map.entry("service-g-deployment", Arrays.asList()));
                break;
            default:
                deploymentGraph = Map.ofEntries(
                        Map.entry("service-a-deployment", Arrays.asList("service-b-deployment")),
                        Map.entry("service-b-deployment", Arrays.asList("service-c-deployment")),
                        Map.entry("service-c-deployment", Arrays.asList("service-d-deployment")),
                        Map.entry("service-d-deployment", Arrays.asList("service-e-deployment")),
                        Map.entry("service-e-deployment", Arrays.asList("service-f-deployment")),
                        Map.entry("service-f-deployment", Arrays.asList("service-g-deployment")),
                        Map.entry("service-g-deployment", Arrays.asList()));
                break;
        }
        SpringApplication.run(Application.class, args);
    }

    public void monitor() {
    }

    @PostMapping(path = "/analyze")
    public void analyze(@RequestBody DeploymentScaleModel requestScaleDetails) {
        System.out.println(String.format("[INFO]Received request from %s...", requestScaleDetails.getName()));
        actionQueue.add(requestScaleDetails);
    }

    @Scheduled(cron = "*/30 * * ? * *")
    public void plan() throws IOException, InterruptedException, URISyntaxException {
        if (actionQueue.size() == 0)
            return;
        DeploymentScaleModel request = actionQueue.remove();

        System.out.println(String.format("[INFO]Planning scaling for %s...", request.getName()));

        Queue<DeploymentScaleModel> actions = new LinkedList<>();
        Queue<String> dependencies = new LinkedList<>();

        dependencies.add(request.getName());

        while (dependencies.size() != 0) {
            String cur_service = dependencies.remove();
            actions.add(new DeploymentScaleModel(cur_service, request.getAction()));

            actionQueue.removeIf(action -> action.getName().equals(cur_service));

            deploymentGraph.get(cur_service).forEach(dep -> dependencies.add(dep));
        }

        execute(actions);
    }

    public void execute(Queue<DeploymentScaleModel> actions)
            throws IOException, InterruptedException, URISyntaxException {
        actions.forEach(action -> {
            HttpRequest request;
            try {
                System.out.println(String.format("[INFO]Sending scaling request to %s...", action.getName()));
                request = HttpRequest.newBuilder()
                        .uri(new URI(
                                String.format("http://localhost:%s/execute?action=%s",
                                        deploymentToPort.get(action.getName()),
                                        action.getAction())))
                        .GET().build();
                httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
            } catch (URISyntaxException e) {
                System.out.println("Error while building request");
            }
        });
    }
}
