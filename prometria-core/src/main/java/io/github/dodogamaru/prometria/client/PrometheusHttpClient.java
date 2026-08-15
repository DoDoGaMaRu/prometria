package io.github.dodogamaru.prometria.client;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.dodogamaru.prometria.exception.PrometheusQueryException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Shared HTTP plumbing for the query handlers and the direct-use API
 * classes: base URL, {@link HttpClient} and JSON mapping.
 *
 * <p>Every component should share one client instance so the requests run
 * over a single connection pool.
 *
 * @author Daehwan Baek
 */
public class PrometheusHttpClient {

    private final String baseUrl;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    /**
     * Creates a client with a default {@link HttpClient} using a 10-second connect timeout.
     *
     * @param baseUrl base URL of the Prometheus HTTP API (e.g. {@code http://localhost:9090/api/v1})
     */
    public PrometheusHttpClient(String baseUrl) {
        this(baseUrl, HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build());
    }

    /**
     * @param baseUrl    base URL of the Prometheus HTTP API (e.g. {@code http://localhost:9090/api/v1})
     * @param httpClient HTTP client used to send requests
     */
    public PrometheusHttpClient(String baseUrl, HttpClient httpClient) {
        this.baseUrl = baseUrl.endsWith("/")
                ? baseUrl.substring(0, baseUrl.length() - 1)
                : baseUrl;
        this.httpClient = httpClient;
        this.objectMapper = new ObjectMapper()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    /**
     * Renders the parameter set as an URL-encoded query string.
     *
     * @param queryParams parameter set, may be {@code null} or empty
     * @return the query string without the leading {@code ?}, or {@code ""}
     */
    private static String renderQuery(QueryParams queryParams) {
        if (queryParams == null) {
            return "";
        }
        List<String> pairs = new ArrayList<>();
        queryParams.toMap().forEach((name, values) ->
                values.forEach(value -> pairs.add(QueryParams.encode(name) + "=" + QueryParams.encode(value))));
        return String.join("&", pairs);
    }

    /**
     * Truncates a response body to 500 characters for use in error messages.
     */
    private static String snippet(String value) {
        if (value == null) {
            return "";
        }
        return value.length() <= 500 ? value : value.substring(0, 500) + "...";
    }

    /**
     * Sends a GET request and maps the JSON response body to the target type.
     *
     * @param path         API path appended to the base URL (e.g. {@code /query})
     * @param queryParams  query parameters, may be {@code null} or empty
     * @param responseType target type of the JSON body
     * @param <T>          result type
     * @return the mapped result
     * @throws PrometheusQueryException if the request fails or the response is not valid JSON
     */
    public <T> T get(String path, QueryParams queryParams, Class<T> responseType) {
        try {
            String query = renderQuery(queryParams);
            String url = baseUrl + path + (query.isEmpty() ? "" : "?" + query);
            HttpRequest request = HttpRequest.newBuilder(URI.create(url)).GET().build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new PrometheusQueryException(
                        "Prometheus query failed. status=" + response.statusCode() + ", body=" + response.body());
            }

            try {
                return objectMapper.readValue(response.body(), responseType);
            } catch (IOException e) {
                throw new PrometheusQueryException(
                        "Prometheus query response is not valid JSON. status=" + response.statusCode()
                                + ", body=" + snippet(response.body()), e);
            }
        } catch (IOException e) {
            throw new PrometheusQueryException("Prometheus query I/O error: " + path, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PrometheusQueryException("Prometheus query interrupted: " + path, e);
        }
    }
}
