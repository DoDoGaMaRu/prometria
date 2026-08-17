package io.github.dodogamaru.prometria.query.instant;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.github.dodogamaru.prometria.model.PrometheusSample;
import io.github.dodogamaru.prometria.model.PrometheusSampleDeserializer;

import java.util.Map;

/**
 * A single series of an instant query result, with its labels and the observed sample.
 *
 * @param metric label set of the series
 * @param value  observed sample
 * @author Daehwan Baek
 */
public record InstantQuerySeries(
        Map<String, String> metric,
        @JsonDeserialize(using = PrometheusSampleDeserializer.class)
        PrometheusSample value
) {

}
