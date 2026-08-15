package io.github.dodogamaru.prometria.model.status;

/**
 * Currently loaded Prometheus configuration.
 *
 * @param yaml the configuration as a dumped YAML file (comments are not included)
 * @author Daehwan Baek
 */
public record StatusConfigData(
        String yaml
) {

}
