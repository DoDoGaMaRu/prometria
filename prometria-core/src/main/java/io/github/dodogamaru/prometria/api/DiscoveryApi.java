package io.github.dodogamaru.prometria.api;

import io.github.dodogamaru.prometria.client.PrometheusHttpClient;
import io.github.dodogamaru.prometria.client.QueryParams;
import io.github.dodogamaru.prometria.model.discovery.LabelNamesResult;
import io.github.dodogamaru.prometria.model.discovery.LabelValuesResult;
import io.github.dodogamaru.prometria.model.discovery.MetadataResult;
import io.github.dodogamaru.prometria.model.discovery.SeriesResult;
import io.github.dodogamaru.prometria.model.time.DateTimeValue;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Direct access to the Prometheus discovery APIs
 * ({@code /series}, {@code /labels}, {@code /label/{name}/values}, {@code /metadata}).
 *
 * <p>These operations take no PromQL, so the API is called with plain
 * arguments instead of going through a {@code @PrometheusRepository} method:
 * <pre>{@code
 * SeriesResult series = discoveryApi.series(List.of("up{job=\"node-exporter\"}"));
 * List<String> jobs = discoveryApi.labelValues("job");
 * }</pre>
 *
 * @author Daehwan Baek
 */
public final class DiscoveryApi {

    private final PrometheusHttpClient http;

    /**
     * @param baseUrl base URL of the Prometheus HTTP API (e.g. {@code http://localhost:9090/api/v1})
     */
    public DiscoveryApi(String baseUrl) {
        this.http = new PrometheusHttpClient(baseUrl);
    }

    /**
     * Renders a time as epoch seconds for use as a query parameter.
     */
    private static Object asEpoch(LocalDateTime time) {
        return time == null ? null : DateTimeValue.of(time);
    }

    /**
     * Runs the series API ({@code /series}).
     *
     * @param match PromQL instance selector list, at least one required by the API
     * @return the query result
     */
    public SeriesResult series(List<String> match) {
        return series(match, null, null);
    }

    /**
     * Runs the series API ({@code /series}).
     *
     * @param match PromQL instance selector list, at least one required by the API
     * @param start start time, may be {@code null}
     * @param end   end time, may be {@code null}
     * @return the query result
     */
    public SeriesResult series(List<String> match, LocalDateTime start, LocalDateTime end) {
        QueryParams params = QueryParams.create()
                .addAll("match[]", match)
                .addIfPresent("start", asEpoch(start))
                .addIfPresent("end", asEpoch(end));
        return http.get("/series", params, SeriesResult.class);
    }

    /**
     * Runs the label names API ({@code /labels}).
     *
     * @return the query result
     */
    public LabelNamesResult labels() {
        return labels(null, null, null);
    }

    /**
     * Runs the label names API ({@code /labels}).
     *
     * @param start start time, may be {@code null}
     * @param end   end time, may be {@code null}
     * @param match PromQL instance selector list, may be {@code null}
     * @return the query result
     */
    public LabelNamesResult labels(LocalDateTime start, LocalDateTime end, List<String> match) {
        QueryParams params = QueryParams.create()
                .addIfPresent("start", asEpoch(start))
                .addIfPresent("end", asEpoch(end))
                .addAll("match[]", match);
        return http.get("/labels", params, LabelNamesResult.class);
    }

    /**
     * Runs the label values API ({@code /label/{name}/values}).
     *
     * @param name label name
     * @return the query result
     */
    public LabelValuesResult labelValues(String name) {
        return labelValues(name, null, null, null);
    }

    /**
     * Runs the label values API ({@code /label/{name}/values}).
     *
     * @param name  label name
     * @param start start time, may be {@code null}
     * @param end   end time, may be {@code null}
     * @param match PromQL instance selector list, may be {@code null}
     * @return the query result
     */
    public LabelValuesResult labelValues(String name, LocalDateTime start, LocalDateTime end, List<String> match) {
        QueryParams params = QueryParams.create()
                .addIfPresent("start", asEpoch(start))
                .addIfPresent("end", asEpoch(end))
                .addAll("match[]", match);
        return http.get("/label/" + QueryParams.encode(name) + "/values", params, LabelValuesResult.class);
    }

    /**
     * Runs the metadata API ({@code /metadata}) for every metric.
     *
     * @return the query result
     */
    public MetadataResult metadata() {
        return metadata(null, null, null);
    }

    /**
     * Runs the metadata API ({@code /metadata}).
     *
     * @param metric metric names to filter by, may be {@code null} for all
     * @return the query result
     */
    public MetadataResult metadata(List<String> metric) {
        return metadata(metric, null, null);
    }

    /**
     * Runs the metadata API ({@code /metadata}).
     *
     * @param metric         metric names to filter by, may be {@code null} for all
     * @param limit          maximum number of metrics to return, may be {@code null}
     * @param limitPerMetric maximum number of help strings per metric, may be {@code null}
     * @return the query result
     */
    public MetadataResult metadata(List<String> metric, Integer limit, Integer limitPerMetric) {
        QueryParams params = QueryParams.create();
        if (metric != null) {
            // Prometheus 2.x reads the filter from `metric[]` while 3.x reads
            // `metric` (first value only). Send both so the filter works on
            // either major version; each server ignores the other's name.
            params.addAll("metric", metric);
            params.addAll("metric[]", metric);
        }
        params.addIfPresent("limit", limit);
        params.addIfPresent("limit_per_metric", limitPerMetric);
        return http.get("/metadata", params, MetadataResult.class);
    }
}
