package io.github.dodogamaru.prometria.api;

import io.github.dodogamaru.prometria.model.status.FeaturesResult;
import io.github.dodogamaru.prometria.model.status.SelfMetricsResult;
import io.github.dodogamaru.prometria.model.status.StatusBuildInfoResult;
import io.github.dodogamaru.prometria.model.status.StatusConfigResult;
import io.github.dodogamaru.prometria.model.status.StatusFlagsResult;
import io.github.dodogamaru.prometria.model.status.StatusRuntimeInfoResult;
import io.github.dodogamaru.prometria.model.status.StatusTsdbBlocksResult;
import io.github.dodogamaru.prometria.model.status.StatusTsdbResult;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies that the status API hits the expected paths with the expected
 * query parameters.
 */
class StatusApiTest {

    private FakePrometheusServer server;
    private StatusApi api;

    @BeforeEach
    void setUp() throws Exception {
        server = new FakePrometheusServer();
        for (String path : new String[]{
                "/status/config", "/status/flags", "/status/runtimeinfo", "/status/buildinfo",
                "/status/tsdb", "/status/tsdb/blocks", "/features"
        }) {
            server.respond(path, "{\"status\":\"success\",\"data\":{}}");
        }
        server.respond("/status/self_metrics", "{\"status\":\"success\",\"data\":[]}");
        api = new StatusApi(server.baseUrl());
    }

    @AfterEach
    void tearDown() {
        server.close();
    }

    @Test
    void configHitsStatusConfigPath() {
        StatusConfigResult result = api.config();

        assertEquals("success", result.status());
        assertNotNull(server.lastRequest("/status/config"));
    }

    @Test
    void flagsHitsStatusFlagsPath() {
        StatusFlagsResult result = api.flags();

        assertEquals("success", result.status());
        assertNotNull(server.lastRequest("/status/flags"));
    }

    @Test
    void runtimeInfoHitsStatusRuntimeInfoPath() {
        StatusRuntimeInfoResult result = api.runtimeInfo();

        assertEquals("success", result.status());
        assertNotNull(server.lastRequest("/status/runtimeinfo"));
    }

    @Test
    void buildInfoHitsStatusBuildInfoPath() {
        StatusBuildInfoResult result = api.buildInfo();

        assertEquals("success", result.status());
        assertNotNull(server.lastRequest("/status/buildinfo"));
    }

    @Test
    void tsdbSendsLimitParameter() {
        StatusTsdbResult result = api.tsdb(3);

        assertEquals("success", result.status());
        assertTrue(server.lastRequest("/status/tsdb").getRawQuery().contains("limit=3"));
    }

    @Test
    void tsdbBlocksHitsStatusTsdbBlocksPath() {
        StatusTsdbBlocksResult result = api.tsdbBlocks();

        assertEquals("success", result.status());
        assertNotNull(server.lastRequest("/status/tsdb/blocks"));
    }

    @Test
    void selfMetricsSendsPatternParameter() {
        SelfMetricsResult result = api.selfMetrics("process_.*");

        assertEquals("success", result.status());
        String query = server.lastRequest("/status/self_metrics").getRawQuery();
        assertNotNull(query);
        assertTrue(query.contains("metric_name_pattern="));
    }

    @Test
    void featuresHitsFeaturesPath() {
        FeaturesResult result = api.features();

        assertEquals("success", result.status());
        assertNotNull(server.lastRequest("/features"));
    }
}
