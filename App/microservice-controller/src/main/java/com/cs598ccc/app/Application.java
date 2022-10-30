package com.cs598ccc.app;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;

@SpringBootApplication
@RestController
@EnableScheduling
public class Application {
    private static final double CPU_ALLOCATION = 0.2;
    private static final double CPU_SCALE_OUT_PERCENT = 0.8;
    private static final double CPU_SCALE_IN_PERCENT = 0.5;
    private static final BigDecimal UPPER_CPU_LIMIT = new BigDecimal(CPU_ALLOCATION * CPU_SCALE_OUT_PERCENT);
    private static final BigDecimal LOWER_CPU_LIMIT = new BigDecimal(CPU_ALLOCATION * CPU_SCALE_IN_PERCENT);
    private BigDecimal cpuUsage = BigDecimal.ZERO;
    private static final int COOL_DOWN_SECONDS = 60;
    private static final int MIN_REPLICA = 1;
    private static final int MAX_REPLICA = 5;

    private static String deploymentName;
    private static Map<String, String> labels;

    private KubernetesClient client = new KubernetesClientBuilder().build();
    private int current_replica = 1;
    private long last_scale_millis;

    private static HttpClient httpClient = HttpClient.newHttpClient();

    public static void main(String[] args) {
        deploymentName = String.format("%s-deployment", args[0]);
        labels = Map.ofEntries(Map.entry("app", String.format("%s-pod", args[0])));
        SpringApplication.run(Application.class, args);
    }

    @Scheduled(cron = "*/15 * * ? * *")
    public void monitor() throws URISyntaxException, IOException, InterruptedException {
        List<BigDecimal> podUsageList = new ArrayList<>();

        client.top().pods().withLabels(labels).metrics("default").getItems()
                .forEach(podMetrics -> podMetrics.getContainers().forEach(container -> {
                    // System.out.println(String.format("Pod: %s -- CPU: %s",
                    // podMetrics.getMetadata().getName(),
                    // container.getUsage().get("cpu").getNumericalAmount()));
                    podUsageList.add(container.getUsage().get("cpu").getNumericalAmount());
                }));

        cpuUsage = podUsageList.stream().reduce(BigDecimal.ZERO, (a, b) -> a.add(b));
        cpuUsage = cpuUsage.divide(new BigDecimal(podUsageList.size()), RoundingMode.HALF_UP);

        current_replica = podUsageList.size();

        System.out.println(String.format("Current CPU Utilization: %s", cpuUsage));

        analyze();
    }

    public void analyze() throws URISyntaxException, IOException, InterruptedException {
        if (cpuUsage.compareTo(UPPER_CPU_LIMIT) >= 0 && current_replica != MAX_REPLICA) {
            System.out.println("Upper CPU usage limit reached, requesting scaling...");
            plan(1);
        }
        if (cpuUsage.compareTo(LOWER_CPU_LIMIT) <= 0 && current_replica != MIN_REPLICA) {
            System.out.println("Lower CPU usage limit reached, requesting scaling...");
            plan(-1);
        }
    }

    public void plan(Integer action) throws URISyntaxException, IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:3000/analyze"))
                .POST(HttpRequest.BodyPublishers
                        .ofString(String.format("{\"name\":\"%s\",\"action\":%s}", deploymentName, action)))
                .setHeader("Content-Type", "application/json")
                .build();
        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }

    @GetMapping(path = "/execute")
    public void execute(@RequestParam(value = "action", defaultValue = "0") Integer action) {
        System.out.println(String.format("[INFO]Handling scaling request to %s", action));
        long current_millis = System.currentTimeMillis();
        int desired_replica = current_replica + action;

        if (current_millis <= (last_scale_millis + COOL_DOWN_SECONDS * 1000)) {
            System.out.println("Scaling requested but still in cool down period");
        } else if (desired_replica == current_replica || desired_replica > MAX_REPLICA || desired_replica < MIN_REPLICA) {
            System.out.println("Unable to scale due to replica count");
        } else {
            last_scale_millis = current_millis;
            client.apps().deployments().inNamespace("default").withName(deploymentName)
                    .scale(desired_replica);
        }
    }
}
