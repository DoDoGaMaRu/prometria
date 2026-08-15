package io.github.dodogamaru.prometria.api;

import io.github.dodogamaru.prometria.model.discovery.ScrapePoolsResult;
import io.github.dodogamaru.prometria.model.rules.AlertmanagersResult;
import io.github.dodogamaru.prometria.model.rules.AlertsResult;
import io.github.dodogamaru.prometria.model.rules.RulesResult;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Verifies that the rules and alerts API hits the expected paths.
 */
class RulesAlertsApiTest {

    private FakePrometheusServer server;
    private RulesAlertsApi api;

    @BeforeEach
    void setUp() throws Exception {
        server = new FakePrometheusServer();
        server.respond("/rules", "{\"status\":\"success\",\"data\":{}}");
        server.respond("/alerts", "{\"status\":\"success\",\"data\":{}}");
        server.respond("/alertmanagers", "{\"status\":\"success\",\"data\":{}}");
        server.respond("/scrape_pools", "{\"status\":\"success\",\"data\":{}}");
        api = new RulesAlertsApi(server.baseUrl());
    }

    @AfterEach
    void tearDown() {
        server.close();
    }

    @Test
    void rulesHitsRulesPath() {
        RulesResult result = api.rules();

        assertEquals("success", result.status());
        assertNotNull(server.lastRequest("/rules"));
    }

    @Test
    void alertsHitsAlertsPath() {
        AlertsResult result = api.alerts();

        assertEquals("success", result.status());
        assertNotNull(server.lastRequest("/alerts"));
    }

    @Test
    void alertmanagersHitsAlertmanagersPath() {
        AlertmanagersResult result = api.alertmanagers();

        assertEquals("success", result.status());
        assertNotNull(server.lastRequest("/alertmanagers"));
    }

    @Test
    void scrapePoolsHitsScrapePoolsPath() {
        ScrapePoolsResult result = api.scrapePools();

        assertEquals("success", result.status());
        assertNotNull(server.lastRequest("/scrape_pools"));
    }
}
