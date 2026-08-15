package io.github.dodogamaru.prometria.model.targets;

import java.util.Map;

/**
 * An active scrape target.
 *
 * @param discoveredLabels   labels before relabeling
 * @param labels             labels after relabeling
 * @param scrapePool         scrape pool (job) name
 * @param scrapeUrl          URL of the target
 * @param globalUrl          externally visible URL of the target
 * @param lastError          last scrape error, or empty
 * @param lastScrape         RFC3339 timestamp of the last scrape
 * @param lastScrapeDuration last scrape duration in seconds
 * @param health             health state ({@code up} | {@code down} | {@code unknown})
 * @param scrapeInterval     configured scrape interval (e.g. {@code 15s})
 * @param scrapeTimeout      configured scrape timeout (e.g. {@code 10s})
 * @author Daehwan Baek
 */
public record Target(
        Map<String, String> discoveredLabels,
        Map<String, String> labels,
        String scrapePool,
        String scrapeUrl,
        String globalUrl,
        String lastError,
        String lastScrape,
        Double lastScrapeDuration,
        String health,
        String scrapeInterval,
        String scrapeTimeout
) {

}
