package io.github.dodogamaru.prometria.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.dodogamaru.prometria.query.instant.InstantQueryResult;
import io.github.dodogamaru.prometria.query.instant.InstantQuerySeries;
import io.github.dodogamaru.prometria.query.range.RangeQueryResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PrometheusResultDeserializationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void queryResultDeserializesVectorResponse() throws Exception {
        String json = """
                {
                  "status": "success",
                  "data": {
                    "resultType": "vector",
                    "result": [
                      {
                        "metric": {
                          "__name__": "up",
                          "job": "application",
                          "instance": "192.168.0.100:12081"
                        },
                        "value": [1786778297.573, "1"]
                      }
                    ]
                  }
                }
                """;

        InstantQueryResult result = objectMapper.readValue(json, InstantQueryResult.class);

        assertEquals("success", result.status());
        assertEquals("vector", result.data().resultType());
        assertEquals(1, result.data().result().size());

        InstantQuerySeries first = result.data().result().getFirst();
        assertEquals("application", first.metric().get("job"));
        assertEquals(1786778297.573, first.value().timestamp(), 0.001);
        assertEquals(1.0, first.value().value(), 0.001);
    }

    @Test
    void queryRangeResultDeserializesMatrixResponse() throws Exception {
        String json = """
                {
                  "status": "success",
                  "data": {
                    "resultType": "matrix",
                    "result": [
                      {
                        "metric": {
                          "__name__": "node_cpu_seconds_total",
                          "job": "node-exporter",
                          "mode": "idle"
                        },
                        "values": [
                          [1786777697.573, "100.5"],
                          [1786777707.573, "101.25"]
                        ]
                      }
                    ]
                  }
                }
                """;

        RangeQueryResult result = objectMapper.readValue(json, RangeQueryResult.class);

        assertEquals("success", result.status());
        assertEquals("matrix", result.data().resultType());

        var values = result.data().result().getFirst().values();
        assertEquals(2, values.size());
        assertEquals(1786777697.573, values.get(0).timestamp(), 0.001);
        assertEquals(100.5, values.get(0).value(), 0.001);
        assertEquals(101.25, values.get(1).value(), 0.001);
    }
}
