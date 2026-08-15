package io.github.dodogamaru.prometria.query.range;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.github.dodogamaru.prometria.model.PrometheusSample;
import io.github.dodogamaru.prometria.model.PrometheusSampleDeserializer;

import java.util.List;
import java.util.Map;

/**
 * A single series of a range query result, with its labels and the samples in the evaluation range.
 *
 * @param metric label set of the series
 * @param values samples in the evaluation range
 * @author Daehwan Baek
 */
public record RangeQuerySeries(
        Map<String, String> metric,
        @JsonDeserialize(contentUsing = PrometheusSampleDeserializer.class)
        List<PrometheusSample> values
) {

}
