package com.cs598ccc.app;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.client.utils.URIBuilder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cs598ccc.app.models.TopologyGraph;
import com.cs598ccc.app.models.TopologyGraph.Vertex;
import com.cs598ccc.app.models.TopologyGraph.Edge;
import com.cs598ccc.app.utils.Utility;

@SpringBootApplication
@RestController
@EnableScheduling
public class Application {
    private static String TOPOLOGY = System.getenv("TOPOLOGY") == null ? "sample"
            : System.getenv("TOPOLOGY");
    private static String MONITOR_ADDR = System.getenv("MONITOR_ADDR") == null ? "localhost:8080"
            : System.getenv("MONITOR_ADDR");
    private static String MONITOR_AVERAGE_PERIOD = System.getenv("MONITOR_AVERAGE_PERIOD") == null ? "10m"
            : System.getenv("MONITOR_AVERAGE_PERIOD");
    private static String MONITOR_ANOMALY_PERIOD = System.getenv("MONITOR_ANOMALY_PERIOD") == null ? "1m"
            : System.getenv("MONITOR_ANOMALY_PERIOD");

    private static TopologyGraph topology = new TopologyGraph();
    private static Map<String, Double> averageResponseTime = new HashMap<>();
    private static Map<String, Double> anomalyResponseTime = new HashMap<>();
    private static Map<String, List<Double>> anomalyResponseTimeStdDev = new HashMap<>();
    private static Map<String, List<Double>> anomalyResponseTimeOverTime = new HashMap<>();

    private static HttpClient httpClient = HttpClient.newHttpClient();
    private static String AVG_RESP_TIME_QUERY = "sum by (kubernetes_name) (rate(http_server_requests_seconds_sum{uri=\"/\"}[%1$s])) / sum by (kubernetes_name) (rate(http_server_requests_seconds_count{uri=\"/\"}[%1$s]))";
    private static String ANOMALY_RESP_TIME_QUERY = "avg_over_time(http_server_requests_seconds{uri=\"/\",quantile=\"0.95\"}[%s])";
    private static HttpRequest avgRequest;
    private static HttpRequest anomalyRequest;

    public static void main(String[] args) throws URISyntaxException {
        instantiate(args);
        SpringApplication.run(Application.class, args);
    }

    /**
     * Separate method to instantiate static variables
     * 
     * @param args
     * @throws URISyntaxException
     */
    public static void instantiate(String[] args) throws URISyntaxException {
        instantiateTopology();
        instantiateAnomalyTracking();

        // Set up uri builder for multiple requests
        URIBuilder ub = new URIBuilder(String.format("http://%s/api/v1/query", MONITOR_ADDR));

        // Build request for average response time
        String avg_query = String.format(AVG_RESP_TIME_QUERY, MONITOR_AVERAGE_PERIOD);
        ub.addParameter("query", avg_query);
        avgRequest = HttpRequest.newBuilder().uri(new URI(ub.toString())).GET().build();

        // Build request for 95th quantile response time
        String anomaly_query = String.format(ANOMALY_RESP_TIME_QUERY, MONITOR_ANOMALY_PERIOD);
        ub.setParameter("query", anomaly_query);
        anomalyRequest = HttpRequest.newBuilder().uri(new URI(ub.toString())).GET().build();
    }

    /**
     * Instantiate topology graph
     */
    public static void instantiateTopology() {
        switch (TOPOLOGY) {
            case "complex":
                topology.addVertex("service-a",
                        new String[] { "service-b", "service-c", "service-d" });
                topology.addVertex("service-b", new String[] { "service-e" });
                topology.addVertex("service-c", new String[] { "service-d", "service-e" });
                topology.addVertex("service-d", new String[] { "service-f" });
                topology.addVertex("service-e");
                topology.addVertex("service-f", new String[] { "service-g" });
                topology.addVertex("service-g");
                break;
            case "diamond":
                topology.addVertex("service-a",
                        new String[] { "service-b", "service-c", "service-d", "service-e", "service-f" });
                topology.addVertex("service-b", new String[] { "service-g" });
                topology.addVertex("service-c", new String[] { "service-g" });
                topology.addVertex("service-d", new String[] { "service-g" });
                topology.addVertex("service-e", new String[] { "service-g" });
                topology.addVertex("service-f", new String[] { "service-g" });
                topology.addVertex("service-g");
                break;
            case "sequential":
                topology.addVertex("service-a", new String[] { "service-b" });
                topology.addVertex("service-b", new String[] { "service-c" });
                topology.addVertex("service-c", new String[] { "service-d" });
                topology.addVertex("service-d", new String[] { "service-e" });
                topology.addVertex("service-e", new String[] { "service-f" });
                topology.addVertex("service-f", new String[] { "service-g" });
                topology.addVertex("service-g");
                break;
            default:
                topology.addVertex("service-a", new String[] { "service-b", "service-c" });
                topology.addVertex("service-b");
                topology.addVertex("service-c");
                break;
        }
    }

    /**
     * Instantiate anomaly tracking map of arraylist to avoid extra logic later
     */
    public static void instantiateAnomalyTracking() {
        anomalyResponseTimeOverTime.put("service-a", new ArrayList<>());
        anomalyResponseTimeOverTime.put("service-b", new ArrayList<>());
        anomalyResponseTimeOverTime.put("service-c", new ArrayList<>());
        anomalyResponseTimeOverTime.put("service-d", new ArrayList<>());
        anomalyResponseTimeOverTime.put("service-e", new ArrayList<>());
        anomalyResponseTimeOverTime.put("service-f", new ArrayList<>());
        anomalyResponseTimeOverTime.put("service-g", new ArrayList<>());
    }

    @GetMapping(path = "/health")
    public String health() {
        return "OK";
    }

    @GetMapping(path = "/status")
    public String status() {
        return "OK";
    }

    /**
     * Monitors the response time using prometheus server
     * Runs every 30 seconds and grabs the average of last minute
     * 
     * @throws InterruptedException
     * @throws IOException
     */
    @Scheduled(cron = "*/30 * * ? * *")
    public void monitorAverageResponseTime() throws IOException, InterruptedException {
        Utility.queryPrometheusReplace(httpClient, avgRequest, averageResponseTime);
    }

    /**
     * Monitors the 95th percentile response time using prometheus server
     * Runs every 30 seconds and grabs the average of last minute
     * 
     * @throws InterruptedException
     * @throws IOException
     */
    @Scheduled(cron = "*/30 * * ? * *")
    public void monitorAnomalyResponseTime() throws IOException, InterruptedException {
        Map<String, Double> updated = Utility.queryPrometheusReplaceAndAppend(httpClient, anomalyRequest,
                anomalyResponseTime,
                anomalyResponseTimeOverTime);

        for (Map.Entry<String, Double> entry : updated.entrySet()) {
            List<Double> bounds = anomalyResponseTimeStdDev.get(entry.getKey());

            if (bounds == null || bounds.size() == 0)
                continue;

            if (entry.getValue() < bounds.get(0) || entry.getValue() > bounds.get(1)) {
                System.out.println(
                        String.format("%s is anomalous with response time of %s", entry.getKey(), entry.getValue()));
            }
        }
    }

    /**
     * Generates the standard deviation of the last 5 minuets of
     * 95th percentile response time, then set the lower and upper bounds
     * of 95th percentile based on 99.7% of values falling within three
     * times the standard deviation
     */
    @Scheduled(cron = "0 0/5 * ? * *")
    public void anomalyAggregation() {
        for (Map.Entry<String, List<Double>> entry : anomalyResponseTimeOverTime.entrySet()) {
            if (entry.getValue().size() == 0)
                continue;

            List<Double> clone = new ArrayList<>(entry.getValue());
            entry.getValue().clear();

            Double avg = clone.stream().mapToDouble(o -> o).average().getAsDouble();
            Double stddev = Utility.calculateStandardDeviation(clone);
            Double lower = avg - 3 * stddev;
            Double upper = avg + 3 * stddev;
            List<Double> bounds = new ArrayList<>();
            bounds.add(0, lower);
            bounds.add(1, upper);
            anomalyResponseTimeStdDev.put(entry.getKey(), bounds);
            System.out.println(
                    String.format("Anomaly bounds for %s, lower: %s, upper: %s", entry.getKey(), lower, upper));
        }
    }

    @GetMapping(path = "/execute")
    public void execute()
            throws URISyntaxException, IOException, InterruptedException {
        for (Entry<Vertex, List<Edge>> entry : topology.adj.entrySet()) {
            String service = entry.getKey().name;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(String.format("http://%s-controller/status", service))).GET()
                    .build();
            HttpResponse response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println(String.format("%s controller ... %s", service, response.statusCode()));
        }
    }
}
