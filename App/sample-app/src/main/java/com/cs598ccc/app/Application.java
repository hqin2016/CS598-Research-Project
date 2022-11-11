package com.cs598ccc.app;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class Application {
	private static int FACTORIAL_BASE = Integer.parseInt(
			System.getenv("FACTORIAL_BASE") == null ? "100" : System.getenv("FACTORIAL_BASE"));
	private static String SERVICE_TYPE = System.getenv("SERVICE_TYPE") == null ? "LEAF" : System.getenv("SERVICE_TYPE");
	private static List<String> DEPENDENCIES = System.getenv("DEPENDENCIES") == null
			? new ArrayList<>()
			: Arrays.asList(System.getenv("DEPENDENCIES").split("\s"));

	private static HttpClient client = HttpClient.newHttpClient();

	public static void main(String[] args) throws URISyntaxException {
		SpringApplication.run(Application.class, args);
	}

	@GetMapping("/health")
	public String health() {
		return "OK";
	}

	@GetMapping("/status")
	public String status() {
		return String.format(
				"%s Service with Factorial %s and Dependencies %s",
				SERVICE_TYPE, FACTORIAL_BASE, DEPENDENCIES);
	}

	@GetMapping("/")
	public String home(@RequestParam(value = "n", defaultValue = "1") String n)
			throws IOException, InterruptedException, URISyntaxException {
		// Generate latency with variations
		factorial(FACTORIAL_BASE + Integer.parseInt(n));

		// If service is a node or has dependency, randomly select dependency to call
		if (SERVICE_TYPE.equals("NODE") || DEPENDENCIES.size() == 0) {
			String dep = DEPENDENCIES.get((int) (Math.random() * DEPENDENCIES.size()));
			HttpRequest request = HttpRequest.newBuilder()
					.uri(new URI(String.format("http://%s?n=%s", dep, n))).GET().build();
			client.send(request, HttpResponse.BodyHandlers.ofString());
		}
		return "OK";
	}

	public static BigInteger factorial(int number) {
		BigInteger factorial = BigInteger.ONE;
		for (int i = number; i > 0; i--) {
			factorial = factorial.multiply(BigInteger.valueOf(i));
		}
		return factorial;
	}
}
