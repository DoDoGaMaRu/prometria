package io.github.dodogamaru.prometria.model.discovery;

/**
 * Metadata of one metric, collected from the {@code HELP} and {@code TYPE}
 * directives of the scraped target.
 *
 * @param type metric type ({@code counter}, {@code gauge}, {@code summary}, {@code histogram}, ...)
 * @param help the HELP text
 * @param unit the unit, or {@code ""} if unknown
 * @author Daehwan Baek
 */
public record MetricMetadata(
        String type,
        String help,
        String unit
) {

}
