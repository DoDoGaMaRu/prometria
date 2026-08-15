package io.github.dodogamaru.prometria.spring;

import io.github.dodogamaru.prometria.client.PrometheusHttpClient;
import io.github.dodogamaru.prometria.query.format.FormatQueryHandler;
import io.github.dodogamaru.prometria.query.instant.InstantQueryHandler;
import io.github.dodogamaru.prometria.query.range.RangeQueryHandler;
import io.github.dodogamaru.prometria.api.DiscoveryApi;
import io.github.dodogamaru.prometria.api.RulesAlertsApi;
import io.github.dodogamaru.prometria.api.StatusApi;
import io.github.dodogamaru.prometria.api.TargetsApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for Prometria.
 *
 * <p>Active only when the {@code prometria.prometheus.base-url} property is set.
 * Registers a shared {@link PrometheusHttpClient}, the built-in
 * {@code QueryHandler} beans for the template-driven operations
 * (query / query_range / format_query), the direct-use API beans of the
 * {@code api} package, and a {@link PrometheusRepositoryScanner} unless
 * the user has defined them.
 *
 * @author Daehwan Baek
 */
@AutoConfiguration
@ConditionalOnProperty("prometria.prometheus.base-url")
public class PrometriaAutoConfiguration {

    /**
     * @return the scanner that registers {@code @PrometheusRepository} beans
     */
    @Bean
    @ConditionalOnMissingBean
    public static PrometheusRepositoryScanner prometheusRepositoryScanner() {
        return new PrometheusRepositoryScanner();
    }

    /**
     * Creates the shared query client from the configured base URL.
     *
     * @param baseUrl base URL of the Prometheus HTTP API (e.g. {@code http://localhost:9090/api/v1})
     * @return the query client
     */
    @Bean
    @ConditionalOnMissingBean
    public PrometheusHttpClient prometheusHttpClient(@Value("${prometria.prometheus.base-url}") String baseUrl) {
        return new PrometheusHttpClient(baseUrl);
    }

    /**
     * @param client shared query client
     * @return the handler for instant queries ({@code /query})
     */
    @Bean
    @ConditionalOnMissingBean
    public InstantQueryHandler instantQueryHandler(PrometheusHttpClient client) {
        return new InstantQueryHandler(client);
    }

    /**
     * @param client shared query client
     * @return the handler for range queries ({@code /query_range})
     */
    @Bean
    @ConditionalOnMissingBean
    public RangeQueryHandler rangeQueryHandler(PrometheusHttpClient client) {
        return new RangeQueryHandler(client);
    }

    /**
     * @param client shared query client
     * @return the handler for the format query API ({@code /format_query})
     */
    @Bean
    @ConditionalOnMissingBean
    public FormatQueryHandler formatQueryHandler(PrometheusHttpClient client) {
        return new FormatQueryHandler(client);
    }

    /**
     * @param baseUrl base URL of the Prometheus HTTP API
     * @return the discovery API ({@code /series}, {@code /labels}, ...)
     */
    @Bean
    @ConditionalOnMissingBean
    public DiscoveryApi discoveryApi(@Value("${prometria.prometheus.base-url}") String baseUrl) {
        return new DiscoveryApi(baseUrl);
    }

    /**
     * @param baseUrl base URL of the Prometheus HTTP API
     * @return the targets API ({@code /targets}, {@code /targets/relabel_steps})
     */
    @Bean
    @ConditionalOnMissingBean
    public TargetsApi targetsApi(@Value("${prometria.prometheus.base-url}") String baseUrl) {
        return new TargetsApi(baseUrl);
    }

    /**
     * @param baseUrl base URL of the Prometheus HTTP API
     * @return the rules and alerts API ({@code /rules}, {@code /alerts}, ...)
     */
    @Bean
    @ConditionalOnMissingBean
    public RulesAlertsApi rulesAlertsApi(@Value("${prometria.prometheus.base-url}") String baseUrl) {
        return new RulesAlertsApi(baseUrl);
    }

    /**
     * @param baseUrl base URL of the Prometheus HTTP API
     * @return the status and diagnostics API ({@code /status/*}, {@code /features})
     */
    @Bean
    @ConditionalOnMissingBean
    public StatusApi statusApi(@Value("${prometria.prometheus.base-url}") String baseUrl) {
        return new StatusApi(baseUrl);
    }
}
