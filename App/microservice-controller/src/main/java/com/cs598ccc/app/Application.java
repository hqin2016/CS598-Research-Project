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
import java.util.Arrays;
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

import com.workday.insights.timeseries.arima.Arima;
import com.workday.insights.timeseries.arima.struct.ForecastResult;
import com.workday.insights.timeseries.arima.struct.ArimaParams;

@SpringBootApplication
@RestController
@EnableScheduling
public class Application {
    private static String SERVICE_NAME = System.getenv("SERVICE_NAME") == null ? "service-a"
            : System.getenv("SERVICE_NAME");

    private static Map<String, String> labels = Map.ofEntries(Map.entry("app", SERVICE_NAME));

    private KubernetesClient k8sClient = new KubernetesClientBuilder().build();
    private HttpClient httpClient = HttpClient.newHttpClient();

    private List<Double> cpuHistory = new ArrayList<>();
    private BigDecimal cpuUsage;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @GetMapping(path = "/health")
    public String health() {
        return "Ok";
    }

    @GetMapping(path = "/status")
    public String status() {
        return "Ok";
    }

    @Scheduled(cron = "*/15 * * ? * *")
    public void monitor() throws URISyntaxException, IOException, InterruptedException {
        List<BigDecimal> podUsageList = new ArrayList<>();
        k8sClient.top().pods().withLabels(labels).metrics("default").getItems()
                .forEach(podMetrics -> podMetrics.getContainers().forEach(container -> {
                    podUsageList.add(container.getUsage().get("cpu").getNumericalAmount());
                }));

        if (podUsageList.size() == 0) {
            return;
        }

        cpuUsage = podUsageList.stream().reduce(BigDecimal.ZERO, (a, b) -> a.add(b));
        cpuUsage = cpuUsage.divide(new BigDecimal(podUsageList.size()), RoundingMode.HALF_UP);

        cpuHistory.add(cpuUsage.doubleValue());

        System.out.println(String.format("Current CPU Utilization: %s", cpuUsage));
    }

    @GetMapping(path = "/analyze")
    public void analyze() {
        ArimaParams params = new ArimaParams(0, 1, 1, 0, 0, 0, 0);
        double[] data = cpuHistory.stream().mapToDouble(Double::doubleValue).toArray();

        System.out.println(String.format("Analyzing %s historical points", data.length));

        ForecastResult forecastResult = Arima.forecast_arima(data, 1, params);
        double[] forecastData = forecastResult.getForecast();

        System.out.println(String.format("Prediction Result: %s", forecastData[0]));
    }

    @GetMapping(path = "/execute")
    public void execute() throws IOException, InterruptedException, URISyntaxException {
        HttpRequest request = HttpRequest.newBuilder().uri(new URI("http://application-controller/health")).GET()
                .build();
        HttpResponse response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println(String.format("Application Controller ... %s", response.statusCode()));
    }

    @GetMapping(path = "/scale")
    public void scale(@RequestParam(value = "replica", defaultValue = "1") Integer replica) {
        System.out.println(String.format("Scaling %s to %s replicas", SERVICE_NAME, replica));
        k8sClient.apps().deployments().inNamespace("default").withName(SERVICE_NAME).scale(replica);
    }
}
