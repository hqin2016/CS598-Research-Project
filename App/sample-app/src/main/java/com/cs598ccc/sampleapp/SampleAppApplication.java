package com.cs598ccc.sampleapp;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class SampleAppApplication {
	private static int FACTORIAL_BASE = Integer.parseInt(
			System.getenv("FACTORIAL_BASE") == null ? "10000" : System.getenv("FACTORIAL_BASE"));
	private static String SERVICE_TYPE = System.getenv("SERVICE_TYPE") == null ? "LEAF" : System.getenv("SERVICE_TYPE");
	private static String HOST_ENDPOINT = System.getenv("HOST_ENDPOINT") == null ? "localhost:8080"
			: System.getenv("HOST_ENDPOINT");
	private static List<String> DEPENDENCIES = System.getenv("DEPENDENCIES") == null
			? Arrays.asList(new String[] { "health" })
			: Arrays.asList(System.getenv("DEPENDENCIES").split("\s"));

	private static HttpClient client = HttpClient.newHttpClient();

	public static void main(String[] args) throws URISyntaxException {
		SpringApplication.run(SampleAppApplication.class, args);
	}

	@GetMapping("/health")
	public String health() {
		return "OK";
	}

	@GetMapping("/status")
	public String status() {
		return String.format("%s Service with Factorial %s and Dependencies %s", SERVICE_TYPE, FACTORIAL_BASE,
				DEPENDENCIES);
	}

	@GetMapping("/")
	public String home(@RequestParam(value = "n", defaultValue = "1") String n)
			throws IOException, InterruptedException, URISyntaxException {
		factorial(FACTORIAL_BASE + Integer.parseInt(n) * 10); // Generate latency with slight variations
		if (SERVICE_TYPE.equals("NODE")) {
			String dep = DEPENDENCIES.get((int) (Math.random() * DEPENDENCIES.size()));
			HttpRequest request = HttpRequest.newBuilder()
					.uri(new URI(String.format("http://%s/%s?n=%s", HOST_ENDPOINT, dep, n))).GET().build();
			Instant start = Instant.now();
			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
			Instant finish = Instant.now();
			long timeElapsed = Duration.between(start, finish).toMillis();
			return String.format("%s:%s|%s", dep, String.valueOf(timeElapsed), response.body().toString());
		}
		return "END:0";
	}

	public static BigInteger factorial(int number) {
		BigInteger factorial = BigInteger.ONE;
		for (int i = number; i > 0; i--) {
			factorial = factorial.multiply(BigInteger.valueOf(i));
		}
		return factorial;
	}
}
