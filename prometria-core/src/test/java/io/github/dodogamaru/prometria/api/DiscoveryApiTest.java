package io.github.dodogamaru.prometria.api;

import io.github.dodogamaru.prometria.model.discovery.LabelNamesResult;
import io.github.dodogamaru.prometria.model.discovery.LabelValuesResult;
import io.github.dodogamaru.prometria.model.discovery.MetadataResult;
import io.github.dodogamaru.prometria.model.discovery.SeriesResult;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies that the discovery API sends the expected path and query
 * parameters and maps the response.
 */
class DiscoveryApiTest {

    private FakePrometheusServer server;
    private DiscoveryApi api;

    private static long epochSeconds(String iso) {
        return LocalDateTime.parse(iso).atZone(java.time.ZoneId.systemDefault()).toEpochSecond();
    }

    @BeforeEach
    void setUp() throws Exception {
        server = new FakePrometheusServer();
        server.respond("/series", "{\"status\":\"success\",\"data\":[]}");
        server.respond("/labels", "{\"status\":\"success\",\"data\":[]}");
        server.respond("/label/job/values", "{\"status\":\"success\",\"data\":[]}");
        server.respond("/metadata", "{\"status\":\"success\",\"data\":{}}");
        api = new DiscoveryApi(server.baseUrl());
    }

    @AfterEach
    void tearDown() {
        server.close();
    }

    @Test
    void seriesSendsMatchParameters() {
        SeriesResult result = api.series(List.of("up{job=\"application\"}"));

        assertEquals("success", result.status());
        URI request = server.lastRequest("/series");
        assertNotNull(request);
        assertTrue(request.getRawQuery().contains("match%5B%5D=up"));
    }

    @Test
    void seriesWithTimeRangeSendsEpochTimes() {
        SeriesResult result = api.series(
                List.of("up"),
                LocalDateTime.of(2026, 1, 1, 0, 0, 0),
                LocalDateTime.of(2026, 1, 2, 0, 0, 0));

        assertEquals("success", result.status());
        String query = server.lastRequest("/series").getRawQuery();
        assertTrue(query.contains("start=" + epochSeconds("2026-01-01T00:00")));
        assertTrue(query.contains("end=" + epochSeconds("2026-01-02T00:00")));
    }

    @Test
    void labelsReturnsResult() {
        LabelNamesResult result = api.labels(LocalDateTime.now(), null, List.of("up"));

        assertEquals("success", result.status());
        assertNotNull(server.lastRequest("/labels"));
    }

    @Test
    void labelValuesResolvesNameIntoPath() {
        LabelValuesResult result = api.labelValues("job");

        assertEquals("success", result.status());
        assertNotNull(server.lastRequest("/label/job/values"));
    }

    @Test
    void metadataSendsBothMetricParameterNames() {
        MetadataResult result = api.metadata(List.of("node_load1"), 10, 2);

        assertEquals("success", result.status());
        String query = server.lastRequest("/metadata").getRawQuery();
        assertTrue(query.contains("metric=node_load1"));
        assertTrue(query.contains("metric%5B%5D=node_load1"));
        assertTrue(query.contains("limit=10"));
        assertTrue(query.contains("limit_per_metric=2"));
    }
}
