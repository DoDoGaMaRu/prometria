package io.github.dodogamaru.prometria.api;

import io.github.dodogamaru.prometria.client.PrometheusHttpClient;
import io.github.dodogamaru.prometria.client.QueryParams;
import io.github.dodogamaru.prometria.model.status.FeaturesResult;
import io.github.dodogamaru.prometria.model.status.SelfMetricsResult;
import io.github.dodogamaru.prometria.model.status.StatusBuildInfoResult;
import io.github.dodogamaru.prometria.model.status.StatusConfigResult;
import io.github.dodogamaru.prometria.model.status.StatusFlagsResult;
import io.github.dodogamaru.prometria.model.status.StatusRuntimeInfoResult;
import io.github.dodogamaru.prometria.model.status.StatusTsdbBlocksResult;
import io.github.dodogamaru.prometria.model.status.StatusTsdbResult;

/**
 * Direct access to the Prometheus status and diagnostics APIs
 * ({@code /status/config}, {@code /status/flags}, {@code /status/runtimeinfo},
 * {@code /status/buildinfo}, {@code /status/tsdb}, {@code /status/tsdb/blocks},
 * {@code /status/self_metrics}, {@code /features}).
 *
 * <pre>{@code
 * StatusBuildInfoResult info = statusApi.buildInfo();
 * String version = info.data().version();
 * }</pre>
 *
 * @author Daehwan Baek
 */
public final class StatusApi {

    private final PrometheusHttpClient http;

    /**
     * @param baseUrl base URL of the Prometheus HTTP API (e.g. {@code http://localhost:9090/api/v1})
     */
    public StatusApi(String baseUrl) {
        this.http = new PrometheusHttpClient(baseUrl);
    }

    /**
     * Runs the config API ({@code /status/config}).
     *
     * @return the query result
     */
    public StatusConfigResult config() {
        return http.get("/status/config", QueryParams.create(), StatusConfigResult.class);
    }

    /**
     * Runs the flags API ({@code /status/flags}).
     *
     * @return the query result
     */
    public StatusFlagsResult flags() {
        return http.get("/status/flags", QueryParams.create(), StatusFlagsResult.class);
    }

    /**
     * Runs the runtime info API ({@code /status/runtimeinfo}).
     *
     * @return the query result
     */
    public StatusRuntimeInfoResult runtimeInfo() {
        return http.get("/status/runtimeinfo", QueryParams.create(), StatusRuntimeInfoResult.class);
    }

    /**
     * Runs the build info API ({@code /status/buildinfo}).
     *
     * @return the query result
     */
    public StatusBuildInfoResult buildInfo() {
        return http.get("/status/buildinfo", QueryParams.create(), StatusBuildInfoResult.class);
    }

    /**
     * Runs the TSDB stats API ({@code /status/tsdb}) without limits.
     *
     * @return the query result
     */
    public StatusTsdbResult tsdb() {
        return tsdb(null);
    }

    /**
     * Runs the TSDB stats API ({@code /status/tsdb}).
     *
     * @param limit maximum number of per-metric entries, may be {@code null}
     * @return the query result
     */
    public StatusTsdbResult tsdb(Integer limit) {
        QueryParams params = QueryParams.create()
                .addIfPresent("limit", limit);
        return http.get("/status/tsdb", params, StatusTsdbResult.class);
    }

    /**
     * Runs the TSDB blocks API ({@code /status/tsdb/blocks}).
     *
     * @return the query result
     */
    public StatusTsdbBlocksResult tsdbBlocks() {
        return http.get("/status/tsdb/blocks", QueryParams.create(), StatusTsdbBlocksResult.class);
    }

    /**
     * Runs the self metrics API ({@code /status/self_metrics}) for every metric.
     *
     * @return the query result
     */
    public SelfMetricsResult selfMetrics() {
        return selfMetrics(null);
    }

    /**
     * Runs the self metrics API ({@code /status/self_metrics}).
     *
     * @param metricNamePattern regular expression matching the metric names to return, may be {@code null}
     * @return the query result
     */
    public SelfMetricsResult selfMetrics(String metricNamePattern) {
        QueryParams params = QueryParams.create()
                .addIfPresent("metric_name_pattern", metricNamePattern);
        return http.get("/status/self_metrics", params, SelfMetricsResult.class);
    }

    /**
     * Runs the features API ({@code /features}).
     *
     * @return the query result
     */
    public FeaturesResult features() {
        return http.get("/features", QueryParams.create(), FeaturesResult.class);
    }
}
