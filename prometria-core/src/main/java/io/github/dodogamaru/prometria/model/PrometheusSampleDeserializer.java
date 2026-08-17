package io.github.dodogamaru.prometria.model;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;

/**
 * Jackson deserializer for {@link PrometheusSample}.
 *
 * <p>Reads the two-element array representation used by the Prometheus HTTP
 * API, {@code [timestamp, "value"]}, into a sample.
 *
 * @author Daehwan Baek
 */
public class PrometheusSampleDeserializer extends JsonDeserializer<PrometheusSample> {

    /**
     * Deserializes a sample from a two-element array.
     *
     * @param p    parser positioned at the sample array
     * @param ctxt deserialization context
     * @return the deserialized sample
     * @throws IOException if the sample cannot be read
     */
    @Override
    public PrometheusSample deserialize(JsonParser p, DeserializationContext ctxt)
            throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        double timestamp = node.get(0).asDouble();
        double value = node.get(1).asDouble();
        return new PrometheusSample(timestamp, value);
    }
}
