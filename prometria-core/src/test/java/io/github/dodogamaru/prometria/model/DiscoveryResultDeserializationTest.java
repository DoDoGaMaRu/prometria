package io.github.dodogamaru.prometria.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.dodogamaru.prometria.model.discovery.LabelNamesResult;
import io.github.dodogamaru.prometria.model.discovery.LabelValuesResult;
import io.github.dodogamaru.prometria.model.discovery.MetadataResult;
import io.github.dodogamaru.prometria.model.discovery.MetricMetadata;
import io.github.dodogamaru.prometria.model.discovery.ScrapePoolsResult;
import io.github.dodogamaru.prometria.model.discovery.SeriesResult;
import io.github.dodogamaru.prometria.model.rules.Alert;
import io.github.dodogamaru.prometria.model.rules.AlertmanagersResult;
import io.github.dodogamaru.prometria.model.rules.AlertsResult;
import io.github.dodogamaru.prometria.model.rules.Rule;
import io.github.dodogamaru.prometria.model.rules.RulesResult;
import io.github.dodogamaru.prometria.model.targets.Target;
import io.github.dodogamaru.prometria.model.targets.TargetsRelabelStepsResult;
import io.github.dodogamaru.prometria.model.targets.TargetsResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DiscoveryResultDeserializationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void seriesResultDeserializesLabelSets() throws Exception {
        String json = """
                {
                  "status": "success",
                  "data": [
                    { "__name__": "up", "instance": "192.168.0.100:12081", "job": "application" },
                    { "__name__": "up", "instance": "node-exporter:9100", "job": "node-exporter" }
                  ]
                }
                """;

        SeriesResult result = objectMapper.readValue(json, SeriesResult.class);

        assertEquals("success", result.status());
        assertEquals(2, result.data().size());
        assertEquals("application", result.data().getFirst().get("job"));
        assertEquals("up", result.data().getFirst().get("__name__"));
    }

    @Test
    void labelNamesResultDeserializesNames() throws Exception {
        String json = """
                { "status": "success", "data": ["__name__", "instance", "job"] }
                """;

        LabelNamesResult result = objectMapper.readValue(json, LabelNamesResult.class);

        assertEquals("success", result.status());
        assertEquals(java.util.List.of("__name__", "instance", "job"), result.data());
    }

    @Test
    void labelValuesResultDeserializesValues() throws Exception {
        String json = """
                { "status": "success", "data": ["application", "node-exporter"] }
                """;

        LabelValuesResult result = objectMapper.readValue(json, LabelValuesResult.class);

        assertEquals("success", result.status());
        assertEquals(java.util.List.of("application", "node-exporter"), result.data());
    }

    @Test
    void metadataResultDeserializesMetricMetadata() throws Exception {
        String json = """
                {
                  "status": "success",
                  "data": {
                    "disk_free_bytes": [
                      { "type": "gauge", "unit": "bytes", "help": "Usable space for path" }
                    ],
                    "go_gc_duration_seconds": [
                      { "type": "summary", "unit": "seconds", "help": "A summary of GC pause durations." }
                    ]
                  }
                }
                """;

        MetadataResult result = objectMapper.readValue(json, MetadataResult.class);

        assertEquals("success", result.status());
        assertTrue(result.data().containsKey("disk_free_bytes"));

        MetricMetadata metadata = result.data().get("disk_free_bytes").getFirst();
        assertEquals("gauge", metadata.type());
        assertEquals("bytes", metadata.unit());
        assertEquals("Usable space for path", metadata.help());
        assertEquals("summary", result.data().get("go_gc_duration_seconds").getFirst().type());
    }

    @Test
    void targetsResultDeserializesActiveAndDroppedTargets() throws Exception {
        String json = """
                {
                  "status": "success",
                  "data": {
                    "activeTargets": [
                      {
                        "discoveredLabels": { "__address__": "node-exporter:9100", "job": "node-exporter" },
                        "labels": { "instance": "node-exporter:9100", "job": "node-exporter" },
                        "scrapePool": "node-exporter",
                        "scrapeUrl": "http://node-exporter:9100/metrics",
                        "globalUrl": "http://node-exporter:9100/metrics",
                        "lastError": "",
                        "lastScrape": "2026-08-16T05:07:13.065516001Z",
                        "lastScrapeDuration": 0.026212714,
                        "health": "up",
                        "scrapeInterval": "15s",
                        "scrapeTimeout": "10s"
                      }
                    ],
                    "droppedTargets": [],
                    "droppedTargetCounts": { "application": 0, "node-exporter": 0 }
                  }
                }
                """;

        TargetsResult result = objectMapper.readValue(json, TargetsResult.class);

        assertEquals("success", result.status());
        assertEquals(1, result.data().activeTargets().size());
        Target target = result.data().activeTargets().getFirst();
        assertEquals("node-exporter", target.scrapePool());
        assertEquals("http://node-exporter:9100/metrics", target.scrapeUrl());
        assertEquals("up", target.health());
        assertEquals("node-exporter:9100", target.labels().get("instance"));
        assertTrue(target.discoveredLabels().containsKey("job"));
        assertTrue(result.data().droppedTargets().isEmpty());
        assertEquals(0, result.data().droppedTargetCounts().get("node-exporter"));
    }

    @Test
    void rulesResultDeserializesGroupsAlertsAndRecordingRules() throws Exception {
        String json = """
                {
                  "status": "success",
                  "data": {
                    "groups": [
                      {
                        "name": "node-alerts",
                        "file": "rules.yml",
                        "interval": 60,
                        "limit": 0,
                        "evaluationTime": 0.001,
                        "lastEvaluation": "2026-08-15T17:00:00Z",
                        "rules": [
                          {
                            "state": "firing",
                            "name": "NodeDown",
                            "query": "up == 0",
                            "duration": 300,
                            "keepFiringFor": 0,
                            "labels": { "severity": "page" },
                            "annotations": { "summary": "node down" },
                            "alerts": [
                              {
                                "labels": { "alertname": "NodeDown", "instance": "node-exporter:9100" },
                                "annotations": { "summary": "node down" },
                                "state": "firing",
                                "activeAt": "2026-08-16T05:00:00Z",
                                "value": "1e+00"
                              }
                            ],
                            "health": "ok",
                            "evaluationTime": 0.0005,
                            "lastEvaluation": "2026-08-16T05:07:13Z",
                            "type": "alerting"
                          },
                          {
                            "name": "node_cpu:avg",
                            "query": "avg by (instance) (1 - node_cpu_seconds_total{mode=\\"idle\\"})",
                            "labels": {},
                            "health": "ok",
                            "evaluationTime": 0.0003,
                            "lastEvaluation": "2026-08-16T05:07:13Z",
                            "type": "recording"
                          }
                        ]
                      }
                    ],
                    "groupNextToken": "rules.yml/node-alerts"
                  }
                }
                """;

        RulesResult result = objectMapper.readValue(json, RulesResult.class);

        assertEquals("success", result.status());
        assertEquals(1, result.data().groups().size());
        assertEquals("rules.yml/node-alerts", result.data().groupNextToken());
        assertEquals(2, result.data().groups().getFirst().rules().size());

        Rule alerting = result.data().groups().getFirst().rules().getFirst();
        assertEquals("alerting", alerting.type());
        assertEquals("firing", alerting.state());
        assertEquals(300.0, alerting.duration());
        assertEquals(1, alerting.alerts().size());
        Alert alert = alerting.alerts().getFirst();
        assertEquals("NodeDown", alert.labels().get("alertname"));
        assertEquals("1e+00", alert.value());

        Rule recording = result.data().groups().getFirst().rules().get(1);
        assertEquals("recording", recording.type());
        assertNull(recording.state());
        assertNull(recording.duration());
        assertNull(recording.alerts());
    }

    @Test
    void alertsResultDeserializesActiveAlerts() throws Exception {
        String json = """
                {
                  "status": "success",
                  "data": {
                    "alerts": [
                      {
                        "labels": { "alertname": "NodeDown", "instance": "node-exporter:9100" },
                        "annotations": {},
                        "state": "firing",
                        "activeAt": "2026-08-16T05:00:00Z",
                        "value": "1e+00"
                      }
                    ]
                  }
                }
                """;

        AlertsResult result = objectMapper.readValue(json, AlertsResult.class);

        assertEquals("success", result.status());
        assertEquals(1, result.data().alerts().size());
        assertEquals("firing", result.data().alerts().getFirst().state());
        assertEquals("NodeDown", result.data().alerts().getFirst().labels().get("alertname"));
    }

    @Test
    void alertmanagersResultDeserializesEndpoints() throws Exception {
        String json = """
                {
                  "status": "success",
                  "data": {
                    "activeAlertmanagers": [
                      { "url": "http://127.0.0.1:9093/api/v1/alerts" }
                    ],
                    "droppedAlertmanagers": []
                  }
                }
                """;

        AlertmanagersResult result = objectMapper.readValue(json, AlertmanagersResult.class);

        assertEquals("success", result.status());
        assertEquals("http://127.0.0.1:9093/api/v1/alerts", result.data().activeAlertmanagers().getFirst().url());
        assertTrue(result.data().droppedAlertmanagers().isEmpty());
    }

    @Test
    void scrapePoolsResultDeserializesPoolNames() throws Exception {
        String json = """
                { "status": "success", "data": { "scrapePools": ["application", "node-exporter"] } }
                """;

        ScrapePoolsResult result = objectMapper.readValue(json, ScrapePoolsResult.class);

        assertEquals("success", result.status());
        assertEquals(java.util.List.of("application", "node-exporter"), result.data().scrapePools());
    }

    @Test
    void relabelStepsResultDeserializesRuleSteps() throws Exception {
        String json = """
                {
                  "status": "success",
                  "data": {
                    "steps": [
                      {
                        "rule": {
                          "source_labels": ["__address__"],
                          "separator": ";",
                          "regex": ".*",
                          "target_label": "__tmp_prometheus_meta_scrape_address",
                          "replacement": "$1",
                          "action": "replace"
                        },
                        "output": {
                          "instance": "node-exporter:9100",
                          "job": "node-exporter"
                        },
                        "keep": true
                      },
                      {
                        "rule": {
                          "source_labels": ["job"],
                          "regex": "blacklisted_.*",
                          "action": "drop"
                        },
                        "output": {
                          "instance": "node-exporter:9100",
                          "job": "node-exporter"
                        },
                        "keep": true
                      }
                    ]
                  }
                }
                """;

        TargetsRelabelStepsResult result = objectMapper.readValue(json, TargetsRelabelStepsResult.class);

        assertEquals("success", result.status());
        assertEquals(2, result.data().steps().size());
        var first = result.data().steps().getFirst();
        assertEquals("replace", first.rule().action());
        assertEquals(java.util.List.of("__address__"), first.rule().sourceLabels());
        assertEquals("node-exporter", first.output().get("job"));
        assertTrue(first.keep());

        var second = result.data().steps().get(1);
        assertEquals("drop", second.rule().action());
        assertEquals(null, second.rule().targetLabel());
    }

    @Test
    void relabelStepsResultAcceptsEmptySteps() throws Exception {
        String json = """
                { "status": "success", "data": { "steps": [] } }
                """;

        TargetsRelabelStepsResult result = objectMapper.readValue(json, TargetsRelabelStepsResult.class);

        assertEquals("success", result.status());
        assertTrue(result.data().steps().isEmpty());
    }
}
