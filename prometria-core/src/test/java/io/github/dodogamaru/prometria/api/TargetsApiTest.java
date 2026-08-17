package io.github.dodogamaru.prometria.api;

import io.github.dodogamaru.prometria.model.targets.TargetsRelabelStepsResult;
import io.github.dodogamaru.prometria.model.targets.TargetsResult;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies that the targets API sends the expected path and query
 * parameters and maps the response.
 */
class TargetsApiTest {

    private FakePrometheusServer server;
    private TargetsApi api;

    @BeforeEach
    void setUp() throws Exception {
        server = new FakePrometheusServer();
        server.respond("/targets", "{\"status\":\"success\",\"data\":{}}");
        server.respond("/targets/relabel_steps", "{\"status\":\"success\",\"data\":{}}");
        api = new TargetsApi(server.baseUrl());
    }

    @AfterEach
    void tearDown() {
        server.close();
    }

    @Test
    void targetsSendsStateAndScrapePool() {
        TargetsResult result = api.targets("active", "node-exporter");

        assertEquals("success", result.status());
        String query = server.lastRequest("/targets").getRawQuery();
        assertTrue(query.contains("state=active"));
        assertTrue(query.contains("scrapePool=node-exporter"));
    }

    @Test
    void relabelStepsSendsScrapePoolAndLabelsJson() {
        TargetsRelabelStepsResult result =
                api.relabelSteps("node-exporter", Map.of("instance", "node:9100"));

        assertEquals("success", result.status());
        String query = server.lastRequest("/targets/relabel_steps").getRawQuery();
        assertNotNull(query);
        assertTrue(query.contains("scrapePool=node-exporter"));
        assertTrue(query.contains("labels="));
    }
}
