package io.github.dodogamaru.prometria.model.status;

/**
 * A label pair of a self metrics sample.
 *
 * @param name  label name
 * @param value label value
 * @author Daehwan Baek
 */
public record SelfMetricsLabel(
        String name,
        String value
) {

}
