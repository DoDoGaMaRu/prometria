package io.github.dodogamaru.prometria.api;

import io.github.dodogamaru.prometria.client.PrometheusHttpClient;
import io.github.dodogamaru.prometria.client.QueryParams;
import io.github.dodogamaru.prometria.model.discovery.ScrapePoolsResult;
import io.github.dodogamaru.prometria.model.rules.AlertmanagersResult;
import io.github.dodogamaru.prometria.model.rules.AlertsResult;
import io.github.dodogamaru.prometria.model.rules.RulesResult;

/**
 * Direct access to the Prometheus rule and alert APIs
 * ({@code /rules}, {@code /alerts}, {@code /alertmanagers}, {@code /scrape_pools}).
 *
 * <pre>{@code
 * RulesResult rules = rulesAlertsApi.rules();
 * }</pre>
 *
 * @author Daehwan Baek
 */
public final class RulesAlertsApi {

    private final PrometheusHttpClient http;

    /**
     * @param baseUrl base URL of the Prometheus HTTP API (e.g. {@code http://localhost:9090/api/v1})
     */
    public RulesAlertsApi(String baseUrl) {
        this.http = new PrometheusHttpClient(baseUrl);
    }

    /**
     * Runs the rules API ({@code /rules}).
     *
     * @return the query result
     */
    public RulesResult rules() {
        return http.get("/rules", QueryParams.create(), RulesResult.class);
    }

    /**
     * Runs the alerts API ({@code /alerts}).
     *
     * @return the query result
     */
    public AlertsResult alerts() {
        return http.get("/alerts", QueryParams.create(), AlertsResult.class);
    }

    /**
     * Runs the alertmanagers API ({@code /alertmanagers}).
     *
     * @return the query result
     */
    public AlertmanagersResult alertmanagers() {
        return http.get("/alertmanagers", QueryParams.create(), AlertmanagersResult.class);
    }

    /**
     * Runs the scrape pools API ({@code /scrape_pools}).
     *
     * @return the query result
     */
    public ScrapePoolsResult scrapePools() {
        return http.get("/scrape_pools", QueryParams.create(), ScrapePoolsResult.class);
    }
}
