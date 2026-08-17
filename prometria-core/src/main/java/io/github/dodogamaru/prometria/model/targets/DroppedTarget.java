package io.github.dodogamaru.prometria.model.targets;

import java.util.Map;

/**
 * A target dropped during service discovery.
 *
 * @param discoveredLabels labels before relabeling
 * @param scrapePool       scrape pool (job) name
 * @author Daehwan Baek
 */
public record DroppedTarget(
        Map<String, String> discoveredLabels,
        String scrapePool
) {

}
