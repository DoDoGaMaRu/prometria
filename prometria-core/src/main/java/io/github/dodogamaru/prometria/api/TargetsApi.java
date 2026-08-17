package io.github.dodogamaru.prometria.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.dodogamaru.prometria.client.PrometheusHttpClient;
import io.github.dodogamaru.prometria.client.QueryParams;
import io.github.dodogamaru.prometria.model.targets.TargetsRelabelStepsResult;
import io.github.dodogamaru.prometria.model.targets.TargetsResult;

import java.util.Map;

/**
 * Direct access to the Prometheus target APIs
 * ({@code /targets}, {@code /targets/relabel_steps}).
 *
 * <pre>{@code
 * TargetsResult targets = targetsApi.targets("active", "node-exporter");
 * }</pre>
 *
 * @author Daehwan Baek
 */
public final class TargetsApi {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final PrometheusHttpClient http;

    /**
     * @param baseUrl base URL of the Prometheus HTTP API (e.g. {@code http://localhost:9090/api/v1})
     */
    public TargetsApi(String baseUrl) {
        this.http = new PrometheusHttpClient(baseUrl);
    }

    /**
     * Runs the targets API ({@code /targets}) without filters.
     *
     * @return the query result
     */
    public TargetsResult targets() {
        return targets(null, null);
    }

    /**
     * Runs the targets API ({@code /targets}).
     *
     * @param state target state ({@code active}, {@code dropped}, {@code all}), may be {@code null}
     * @return the query result
     */
    public TargetsResult targets(String state) {
        return targets(state, null);
    }

    /**
     * Runs the targets API ({@code /targets}).
     *
     * @param state      target state ({@code active}, {@code dropped}, {@code all}), may be {@code null}
     * @param scrapePool scrape pool to filter by, may be {@code null}
     * @return the query result
     */
    public TargetsResult targets(String state, String scrapePool) {
        QueryParams params = QueryParams.create()
                .addIfPresent("state", state)
                .addIfPresent("scrapePool", scrapePool);
        return http.get("/targets", params, TargetsResult.class);
    }

    /**
     * Runs the target relabel steps API ({@code /targets/relabel_steps}).
     *
     * @param scrapePool scrape pool to inspect, required
     * @param labels     label set to run the relabel rules on, required
     * @return the query result
     */
    public TargetsRelabelStepsResult relabelSteps(String scrapePool, Map<String, String> labels) {
        QueryParams params = QueryParams.create().addIfPresent("scrapePool", scrapePool);
        try {
            params.add("labels", MAPPER.writeValueAsString(labels));
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("labels must be serializable to JSON", e);
        }
        return http.get("/targets/relabel_steps", params, TargetsRelabelStepsResult.class);
    }
}
