package io.github.dodogamaru.prometria.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.dodogamaru.prometria.model.status.FeaturesResult;
import io.github.dodogamaru.prometria.model.status.SelfMetricsResult;
import io.github.dodogamaru.prometria.model.status.StatusBuildInfoResult;
import io.github.dodogamaru.prometria.model.status.StatusConfigResult;
import io.github.dodogamaru.prometria.model.status.StatusFlagsResult;
import io.github.dodogamaru.prometria.model.status.StatusRuntimeInfoResult;
import io.github.dodogamaru.prometria.model.status.StatusTsdbBlocksResult;
import io.github.dodogamaru.prometria.model.status.StatusTsdbResult;
import io.github.dodogamaru.prometria.query.format.FormatQueryResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StatusResultDeserializationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void statusConfigResultDeserializesYaml() throws Exception {
        String json = """
                {
                  "status": "success",
                  "data": { "yaml": "global:\\n  scrape_interval: 15s\\nscrape_configs: []\\n" }
                }
                """;

        StatusConfigResult result = objectMapper.readValue(json, StatusConfigResult.class);

        assertEquals("success", result.status());
        assertTrue(result.data().yaml().contains("scrape_interval"));
    }

    @Test
    void statusFlagsResultDeserializesFlagMap() throws Exception {
        String json = """
                {
                  "status": "success",
                  "data": { "log.level": "info", "query.timeout": "2m", "storage.tsdb.path": "/prometheus" }
                }
                """;

        StatusFlagsResult result = objectMapper.readValue(json, StatusFlagsResult.class);

        assertEquals("success", result.status());
        assertEquals("info", result.data().get("log.level"));
        assertEquals("2m", result.data().get("query.timeout"));
    }

    @Test
    void statusRuntimeInfoResultDeserializesRuntimeProperties() throws Exception {
        String json = """
                {
                  "status": "success",
                  "data": {
                    "startTime": "2026-08-15T07:11:42.013848113Z",
                    "CWD": "/prometheus",
                    "hostname": "681d169d7dbf",
                    "serverTime": "2026-08-16T05:07:25.204499946Z",
                    "reloadConfigSuccess": true,
                    "lastConfigTime": "2026-08-15T07:11:42Z",
                    "corruptionCount": 0,
                    "goroutineCount": 35,
                    "GOMAXPROCS": 4,
                    "GOMEMLIMIT": 11040517324,
                    "GOGC": "75",
                    "GODEBUG": "",
                    "storageRetention": "15d"
                  }
                }
                """;

        StatusRuntimeInfoResult result = objectMapper.readValue(json, StatusRuntimeInfoResult.class);

        assertEquals("success", result.status());
        assertEquals("/prometheus", result.data().cwd());
        assertEquals("681d169d7dbf", result.data().hostname());
        assertTrue(result.data().reloadConfigSuccess());
        assertEquals(0, result.data().corruptionCount());
        assertEquals(35, result.data().goroutineCount());
        assertEquals(4, result.data().gomaxprocs());
        assertEquals(11040517324L, result.data().goMemLimit());
        assertEquals("75", result.data().gogc());
        assertEquals("15d", result.data().storageRetention());
    }

    @Test
    void statusBuildInfoResultDeserializesBuildProperties() throws Exception {
        String json = """
                {
                  "status": "success",
                  "data": {
                    "version": "3.5.0",
                    "revision": "8be3a9560fbdd18a94dedec4b747c35178177202",
                    "branch": "HEAD",
                    "buildUser": "root@4451b64cb451",
                    "buildDate": "20250714-16:15:23",
                    "goVersion": "go1.24.5"
                  }
                }
                """;

        StatusBuildInfoResult result = objectMapper.readValue(json, StatusBuildInfoResult.class);

        assertEquals("success", result.status());
        assertEquals("3.5.0", result.data().version());
        assertEquals("go1.24.5", result.data().goVersion());
    }

    @Test
    void statusTsdbResultDeserializesCardinalityStats() throws Exception {
        String json = """
                {
                  "status": "success",
                  "data": {
                    "headStats": {
                      "numSeries": 852,
                      "numLabelPairs": 707,
                      "chunkCount": 2556,
                      "minTime": 1786852805303,
                      "maxTime": 1786857575300
                    },
                    "seriesCountByMetricName": [
                      { "name": "node_cpu_seconds_total", "value": 32 }
                    ],
                    "labelValueCountByLabelName": [
                      { "name": "__name__", "value": 442 }
                    ],
                    "memoryInBytesByLabelName": [
                      { "name": "__name__", "value": 34258 }
                    ],
                    "seriesCountByLabelValuePair": [
                      { "name": "instance=node-exporter:9100", "value": 602 }
                    ]
                  }
                }
                """;

        StatusTsdbResult result = objectMapper.readValue(json, StatusTsdbResult.class);

        assertEquals("success", result.status());
        assertEquals(852, result.data().headStats().numSeries());
        assertEquals(707, result.data().headStats().numLabelPairs());
        assertEquals(2556, result.data().headStats().chunkCount());
        assertEquals(1786852805303L, result.data().headStats().minTime());
        assertEquals("node_cpu_seconds_total", result.data().seriesCountByMetricName().getFirst().name());
        assertEquals(32, result.data().seriesCountByMetricName().getFirst().value());
        assertEquals("instance=node-exporter:9100", result.data().seriesCountByLabelValuePair().getFirst().name());
    }

    @Test
    void statusTsdbBlocksResultDeserializesBlockMetadata() throws Exception {
        String json = """
                {
                  "status": "success",
                  "data": {
                    "blocks": [
                      {
                        "ulid": "0F8ZS3V5WQ0K8J8K6Q0K8J8K6Q",
                        "minTime": 1786771200000,
                        "maxTime": 1786857600000,
                        "stats": {
                          "numSamples": 12345,
                          "numFloatSamples": 12000,
                          "numHistogramSamples": 345,
                          "numSeries": 852,
                          "numChunks": 2556,
                          "numTombstones": 0
                        },
                        "compaction": {
                          "level": 3,
                          "sources": [],
                          "deletable": false,
                          "parents": [
                            { "ulid": "0F8ZS3V5WQ0K8J8K6Q0K8J8K6A", "minTime": 1786771200000, "maxTime": 1786792800000 }
                          ],
                          "failed": false,
                          "hints": []
                        },
                        "version": 2
                      }
                    ]
                  }
                }
                """;

        StatusTsdbBlocksResult result = objectMapper.readValue(json, StatusTsdbBlocksResult.class);

        assertEquals("success", result.status());
        assertEquals(1, result.data().blocks().size());
        var block = result.data().blocks().getFirst();
        assertEquals("0F8ZS3V5WQ0K8J8K6Q0K8J8K6Q", block.ulid());
        assertEquals(1786771200000L, block.minTime());
        assertEquals(12345L, block.stats().numSamples());
        assertEquals(3, block.compaction().level());
        assertEquals("0F8ZS3V5WQ0K8J8K6Q0K8J8K6A", block.compaction().parents().getFirst().ulid());
        assertEquals(2, block.version());
    }

    @Test
    void statusTsdbBlocksResultToleratesAbsentOptionalStats() throws Exception {
        String json = """
                {
                  "status": "success",
                  "data": {
                    "blocks": [
                      {
                        "ulid": "0F8ZS3V5WQ0K8J8K6Q0K8J8K6Q",
                        "minTime": 1786771200000,
                        "maxTime": 1786857600000,
                        "compaction": { "level": 1 },
                        "version": 2
                      }
                    ]
                  }
                }
                """;

        StatusTsdbBlocksResult result = objectMapper.readValue(json, StatusTsdbBlocksResult.class);

        var block = result.data().blocks().getFirst();
        assertEquals(null, block.stats());
        assertEquals(1, block.compaction().level());
        assertEquals(null, block.compaction().parents());
    }

    @Test
    void selfMetricsResultDeserializesProtoJsonFamilies() throws Exception {
        String json = """
                {
                  "status": "success",
                  "data": [
                    {
                      "name": "go_goroutines",
                      "help": "Number of OS threads created in the Golang runtime.",
                      "type": "GAUGE",
                      "metric": [
                        { "gauge": { "value": 35.0 } }
                      ]
                    },
                    {
                      "name": "prometheus_http_handler_duration_seconds",
                      "help": "Histogram of latencies for the HTTP handler.",
                      "type": "HISTOGRAM",
                      "metric": [
                        {
                          "label": [
                            { "name": "handler", "value": "api_v1_query" },
                            { "name": "status_code", "value": "200" }
                          ],
                          "histogram": {
                            "sampleCount": "1200",
                            "sampleSum": 34.5
                          }
                        }
                      ]
                    }
                  ]
                }
                """;

        SelfMetricsResult result = objectMapper.readValue(json, SelfMetricsResult.class);

        assertEquals("success", result.status());
        assertEquals(2, result.data().size());
        assertEquals("go_goroutines", result.data().getFirst().name());
        assertEquals("GAUGE", result.data().getFirst().type());
        assertEquals(35.0, result.data().getFirst().metric().getFirst().gauge().get("value").asDouble(), 0.001);

        var histogramSample = result.data().get(1).metric().getFirst();
        assertEquals("api_v1_query", histogramSample.label().get(0).value());
        assertEquals("1200", histogramSample.histogram().get("sampleCount").asText());
    }

    @Test
    void featuresResultDeserializesFeatureFlagMap() throws Exception {
        String json = """
                {
                  "status": "success",
                  "data": {
                    "api": { "experimental-search-api": false, "experimental-status-names": false },
                    "promql": { "extended-lookback-deltas": false },
                    "tsdb": { "out-of-order-tsdb": true }
                  }
                }
                """;

        FeaturesResult result = objectMapper.readValue(json, FeaturesResult.class);

        assertEquals("success", result.status());
        assertEquals(false, result.data().get("api").get("experimental-search-api"));
        assertEquals(true, result.data().get("tsdb").get("out-of-order-tsdb"));
    }

    @Test
    void formatQueryResultDeserializesFormattedString() throws Exception {
        String json = """
                { "status": "success", "data": "foo / bar" }
                """;

        FormatQueryResult result = objectMapper.readValue(json, FormatQueryResult.class);

        assertEquals("success", result.status());
        assertEquals("foo / bar", result.data());
    }
}
