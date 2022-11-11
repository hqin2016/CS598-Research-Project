package com.cs598ccc.app.utils;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cs598ccc.app.models.PrometheusResponse;
import com.cs598ccc.app.models.Result;
import com.google.gson.Gson;

public final class Utility {
    private static Gson gson = new Gson();

    private Utility() throws Exception {
        throw new Exception();
    }

    public static double calculateStandardDeviation(List<Double> list) {
        double avg = list.stream().mapToDouble(o -> o).average().getAsDouble();
        double dev = list.stream().mapToDouble(o -> Math.pow((o - avg), 2)).sum();
        dev = dev / list.size();
        dev = Math.sqrt(dev);

        return dev;
    }

    public static Map<String, Double> queryPrometheusReplace(HttpClient client, HttpRequest request,
            Map<String, Double> storage)
            throws IOException, InterruptedException {

        Map<String, Double> updated = new HashMap<>();

        HttpResponse<String> res = client.send(request, HttpResponse.BodyHandlers.ofString());
        PrometheusResponse resObj = gson.fromJson(res.body().toString(), PrometheusResponse.class);
        for (Result result : resObj.getData().getResult()) {
            String serviceName = result.getMetric().getKubernetesName();
            Double responseTime = result.getValue().get(1).doubleValue();

            System.out.println(String.format("%s average: %s", serviceName, responseTime));

            if (!responseTime.isNaN()) {
                storage.put(serviceName, responseTime);
                updated.put(serviceName, responseTime);
            }
        }

        return updated;
    }

    public static Map<String, Double> queryPrometheusReplaceAndAppend(HttpClient client, HttpRequest request,
            Map<String, Double> storageA,
            Map<String, List<Double>> storageB)
            throws IOException, InterruptedException {
        Map<String, Double> updated = new HashMap<>();

        HttpResponse<String> res = client.send(request, HttpResponse.BodyHandlers.ofString());
        PrometheusResponse resObj = gson.fromJson(res.body().toString(), PrometheusResponse.class);
        for (Result result : resObj.getData().getResult()) {
            String serviceName = result.getMetric().getKubernetesName();
            Double responseTime = result.getValue().get(1).doubleValue();

            System.out.println(String.format("%s 95th percentile: %s", serviceName, responseTime));

            if (!responseTime.isNaN()) {
                storageA.put(serviceName, responseTime);
                storageB.get(serviceName).add(responseTime);
                updated.put(serviceName, responseTime);
            }
        }

        return updated;
    }
}
