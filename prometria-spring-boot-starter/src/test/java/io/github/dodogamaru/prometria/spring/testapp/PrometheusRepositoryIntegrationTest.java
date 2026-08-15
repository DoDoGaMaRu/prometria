package io.github.dodogamaru.prometria.spring.testapp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.github.dodogamaru.prometria.model.rules.AlertmanagersResult;
import io.github.dodogamaru.prometria.model.rules.AlertsResult;
import io.github.dodogamaru.prometria.model.status.FeaturesResult;
import io.github.dodogamaru.prometria.model.discovery.LabelNamesResult;
import io.github.dodogamaru.prometria.model.discovery.LabelValuesResult;
import io.github.dodogamaru.prometria.model.discovery.MetadataResult;
import io.github.dodogamaru.prometria.query.format.FormatQueryCondition;
import io.github.dodogamaru.prometria.query.format.FormatQueryResult;
import io.github.dodogamaru.prometria.query.instant.InstantQueryCondition;
import io.github.dodogamaru.prometria.query.instant.InstantQueryResult;
import io.github.dodogamaru.prometria.query.range.RangeQueryCondition;
import io.github.dodogamaru.prometria.query.range.RangeQueryResult;
import io.github.dodogamaru.prometria.model.rules.RulesResult;
import io.github.dodogamaru.prometria.model.discovery.ScrapePoolsResult;
import io.github.dodogamaru.prometria.model.status.StatusBuildInfoResult;
import io.github.dodogamaru.prometria.model.status.StatusConfigResult;
import io.github.dodogamaru.prometria.model.status.StatusFlagsResult;
import io.github.dodogamaru.prometria.model.status.StatusRuntimeInfoResult;
import io.github.dodogamaru.prometria.model.status.SelfMetricsResult;
import io.github.dodogamaru.prometria.model.status.StatusTsdbResult;
import io.github.dodogamaru.prometria.model.status.StatusTsdbBlocksResult;
import io.github.dodogamaru.prometria.model.targets.TargetsResult;
import io.github.dodogamaru.prometria.model.targets.TargetsRelabelStepsResult;
import io.github.dodogamaru.prometria.api.DiscoveryApi;
import io.github.dodogamaru.prometria.api.RulesAlertsApi;
import io.github.dodogamaru.prometria.api.StatusApi;
import io.github.dodogamaru.prometria.api.TargetsApi;
import io.github.dodogamaru.prometria.model.time.TimeUnit;
import io.github.dodogamaru.prometria.model.time.TimeValue;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("integration")
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
// repository bean is registered at runtime by PrometheusRepositoryScanner;
// the API beans are registered by the auto-configuration
@SpringBootTest(
        classes = PrometriaTestApplication.class,
        properties = "prometria.prometheus.base-url=http://localhost:11999/api/v1"
)
class PrometheusRepositoryIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(PrometheusRepositoryIntegrationTest.class);
    private final ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    @Autowired
    private PrometheusIntegrationRepository repository;
    @Autowired
    private DiscoveryApi discoveryApi;
    @Autowired
    private TargetsApi targetsApi;
    @Autowired
    private RulesAlertsApi rulesAlertsApi;
    @Autowired
    private StatusApi statusApi;

    @Test
    void instantQueryReturnsUpMetricForApplicationJob() {
        InstantQueryResult result = repository.up("application", InstantQueryCondition.builder().build());

        assertEquals("success", result.status());
        assertEquals("vector", result.data().resultType());
        assertFalse(result.data().result().isEmpty());
        assertNotNull(result.data().result().getFirst().metric().get("instance"));

        log.info("instant query result:\n{}", toJson(result));
    }

    @Test
    void rangeQueryReturnsCpuSamplesForNodeExporterJob() {
        LocalDateTime end = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        RangeQueryCondition condition = RangeQueryCondition.builder()
                .start(end.minusHours(1))
                .end(end)
                .step(TimeValue.of(15L, TimeUnit.SECONDS))
                .build();

        RangeQueryResult result = repository.cpuUsageByMode("node-exporter", condition);

        assertEquals("success", result.status());
        assertEquals("matrix", result.data().resultType());
        assertFalse(result.data().result().isEmpty());
        assertFalse(result.data().result().getFirst().values().isEmpty());

        log.info("range query result:\n{}", toJson(result));
    }

    @Test
    void labelNamesReturnsConfiguredLabels() {
        LabelNamesResult result = discoveryApi.labels();

        assertEquals("success", result.status());
        assertTrue(result.data().contains("job"));
        assertTrue(result.data().contains("instance"));

        log.info("labels result:\n{}", toJson(result));
    }

    @Test
    void labelValuesReturnsJobNames() {
        LabelValuesResult result = discoveryApi.labelValues("job");

        assertEquals("success", result.status());
        assertTrue(result.data().contains("application"));
        assertTrue(result.data().contains("node-exporter"));

        log.info("values result:\n{}", toJson(result));
    }

    @Test
    void metadataFilterRestrictsToRequestedMetric() {
        MetadataResult filtered = discoveryApi.metadata(java.util.List.of("node_load1"));
        MetadataResult unfiltered = discoveryApi.metadata();

        assertEquals("success", filtered.status());
        assertEquals(1, filtered.data().size());
        assertTrue(filtered.data().containsKey("node_load1"));
        assertTrue(filtered.data().size() < unfiltered.data().size());

        log.info("filtered metadata result:\n{}", toJson(filtered));
    }

    @Test
    void targetsReturnsActiveScrapeTargets() {
        TargetsResult result = targetsApi.targets("active");

        assertEquals("success", result.status());
        assertFalse(result.data().activeTargets().isEmpty());
        assertEquals("up", result.data().activeTargets().getFirst().health());
        assertTrue(result.data().droppedTargets().isEmpty());

        log.info("targets result:\n{}", toJson(result));
    }

    @Test
    void targetsCanBeFilteredByScrapePool() {
        TargetsResult result = targetsApi.targets(null, "node-exporter");

        assertEquals("success", result.status());
        assertFalse(result.data().activeTargets().isEmpty());
        assertTrue(result.data().activeTargets()
                .stream()
                .allMatch(target -> "node-exporter".equals(target.scrapePool())));

        log.info("targets (node-exporter) result:\n{}", toJson(result));
    }

    @Test
    void rulesReturnsRuleGroups() {
        RulesResult result = rulesAlertsApi.rules();

        assertEquals("success", result.status());
        assertNotNull(result.data().groups());

        log.info("rules result:\n{}", toJson(result));
    }

    @Test
    void alertsReturnsActiveAlerts() {
        AlertsResult result = rulesAlertsApi.alerts();

        assertEquals("success", result.status());
        assertNotNull(result.data().alerts());

        log.info("alerts result:\n{}", toJson(result));
    }

    @Test
    void alertmanagersReturnsDiscoveredEndpoints() {
        AlertmanagersResult result = rulesAlertsApi.alertmanagers();

        assertEquals("success", result.status());
        assertNotNull(result.data().activeAlertmanagers());

        log.info("alertmanagers result:\n{}", toJson(result));
    }

    @Test
    void scrapePoolsReturnsConfiguredPools() {
        ScrapePoolsResult result = rulesAlertsApi.scrapePools();

        assertEquals("success", result.status());
        assertTrue(result.data().scrapePools().contains("application"));
        assertTrue(result.data().scrapePools().contains("node-exporter"));

        log.info("scrape pools result:\n{}", toJson(result));
    }

    @Test
    void statusConfigReturnsLoadedYaml() {
        StatusConfigResult result = statusApi.config();

        assertEquals("success", result.status());
        assertTrue(result.data().yaml().contains("scrape_configs"));

        log.info("status config result:\n{}", toJson(result));
    }

    @Test
    void statusFlagsReturnsFlagMap() {
        StatusFlagsResult result = statusApi.flags();

        assertEquals("success", result.status());
        assertTrue(result.data().containsKey("query.timeout"));

        log.info("status flags result:\n{}", toJson(result));
    }

    @Test
    void statusRuntimeInfoReturnsRuntimeProperties() {
        StatusRuntimeInfoResult result = statusApi.runtimeInfo();

        assertEquals("success", result.status());
        assertNotNull(result.data().startTime());
        assertEquals("15d", result.data().storageRetention());

        log.info("status runtime info result:\n{}", toJson(result));
    }

    @Test
    void statusBuildInfoReturnsBuildProperties() {
        StatusBuildInfoResult result = statusApi.buildInfo();

        assertEquals("success", result.status());
        assertEquals("3.13.2", result.data().version());
        assertEquals("go1.26.5", result.data().goVersion());

        log.info("status build info result:\n{}", toJson(result));
    }

    @Test
    void statusTsdbReturnsCardinalityStats() {
        StatusTsdbResult result = statusApi.tsdb(3);

        assertEquals("success", result.status());
        assertTrue(result.data().headStats().numSeries() > 0);
        assertTrue(result.data().seriesCountByMetricName().size() <= 3);

        log.info("status tsdb result:\n{}", toJson(result));
    }

    @Test
    void formatQueryReturnsPrettifiedExpression() {
        FormatQueryResult result = repository.formatQuery(new FormatQueryCondition());

        assertEquals("success", result.status());
        assertEquals("avg without (mode) (node_load1)", result.data());

        log.info("format query result:\n{}", toJson(result));
    }

    @Test
    void featuresReturnsEnabledFeatureFlagMap() {
        FeaturesResult result = statusApi.features();

        assertEquals("success", result.status());
        assertTrue(result.data().containsKey("api"));
        assertTrue(result.data().containsKey("promql"));

        log.info("features result:\n{}", toJson(result));
    }

    @Test
    void statusTsdbBlocksReturnsCompactedBlocks() {
        StatusTsdbBlocksResult result = statusApi.tsdbBlocks();

        assertEquals("success", result.status());
        assertNotNull(result.data().blocks());
        result.data().blocks().forEach(block -> assertNotNull(block.ulid()));

        log.info("status tsdb blocks result:\n{}", toJson(result));
    }

    @Test
    void selfMetricsReturnsFilteredGoMetrics() {
        SelfMetricsResult result = statusApi.selfMetrics("^go_goroutines$");

        assertEquals("success", result.status());
        assertEquals(1, result.data().size());
        assertEquals("go_goroutines", result.data().getFirst().name());

        log.info("self metrics result:\n{}", toJson(result));
    }

    @Test
    void targetsRelabelStepsReturnsRuleStepsForScrapePool() {
        TargetsRelabelStepsResult result = targetsApi.relabelSteps(
                "node-exporter",
                java.util.Map.of("instance", "node-exporter:9100", "job", "node-exporter"));

        assertEquals("success", result.status());
        assertNotNull(result.data().steps());

        log.info("targets relabel steps result:\n{}", toJson(result));
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize query result", e);
        }
    }
}
