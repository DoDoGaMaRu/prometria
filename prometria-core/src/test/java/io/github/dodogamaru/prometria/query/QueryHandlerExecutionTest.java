package io.github.dodogamaru.prometria.query;

import io.github.dodogamaru.prometria.api.FakePrometheusServer;
import io.github.dodogamaru.prometria.client.PrometheusHttpClient;
import io.github.dodogamaru.prometria.model.time.TimeUnit;
import io.github.dodogamaru.prometria.model.time.TimeValue;
import io.github.dodogamaru.prometria.query.format.FormatQueryCondition;
import io.github.dodogamaru.prometria.query.format.FormatQueryHandler;
import io.github.dodogamaru.prometria.query.format.FormatQueryResult;
import io.github.dodogamaru.prometria.query.instant.InstantQueryCondition;
import io.github.dodogamaru.prometria.query.instant.InstantQueryHandler;
import io.github.dodogamaru.prometria.query.instant.InstantQueryResult;
import io.github.dodogamaru.prometria.query.range.RangeQueryCondition;
import io.github.dodogamaru.prometria.query.range.RangeQueryHandler;
import io.github.dodogamaru.prometria.query.range.RangeQueryResult;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Executes every built-in template-driven query handler against a fake
 * Prometheus server and verifies the path, the query parameter wiring, and
 * the response mapping.
 */
class QueryHandlerExecutionTest {

    private FakePrometheusServer server;
    private PrometheusHttpClient client;

    @BeforeEach
    void setUp() throws Exception {
        server = new FakePrometheusServer();
        server.respond("/query", "{\"status\":\"success\",\"data\":{\"resultType\":\"vector\",\"result\":[]}}");
        server.respond("/query_range", "{\"status\":\"success\",\"data\":{\"resultType\":\"matrix\",\"result\":[]}}");
        server.respond("/format_query", "{\"status\":\"success\",\"data\":\"\"}");
        client = new PrometheusHttpClient(server.baseUrl());
    }

    @AfterEach
    void tearDown() {
        server.close();
    }

    @Test
    void instantQuerySendsQueryAndTime() {
        InstantQueryResult result = new InstantQueryHandler(client).getResult(
                "up", InstantQueryCondition.builder().time(LocalDateTime.of(2026, 1, 1, 0, 0)).build());

        assertEquals("success", result.status());
        String query = server.lastRequest("/query").getRawQuery();
        assertTrue(query.contains("query=up"));
        assertTrue(query.contains("time="));
    }

    @Test
    void rangeQuerySendsRangeParameters() {
        RangeQueryResult result = new RangeQueryHandler(client).getResult(
                "up", RangeQueryCondition.builder()
                        .start(LocalDateTime.of(2026, 1, 1, 0, 0))
                        .end(LocalDateTime.of(2026, 1, 1, 1, 0))
                        .step(TimeValue.of(15L, TimeUnit.SECONDS))
                        .build());

        assertEquals("success", result.status());
        String query = server.lastRequest("/query_range").getRawQuery();
        assertTrue(query.contains("query=up"));
        assertTrue(query.contains("start="));
        assertTrue(query.contains("end="));
        assertTrue(query.contains("step=15s"));
    }

    @Test
    void formatQueryFormatsTheTemplate() {
        FormatQueryResult result = new FormatQueryHandler(client).getResult(
                "sum(up)", new FormatQueryCondition());

        assertEquals("success", result.status());
        assertTrue(server.lastRequest("/format_query").getRawQuery().contains("query=sum%28up%29"));
    }
}
