package io.github.dodogamaru.prometria.model.discovery;

import java.util.List;

/**
 * Configured scrape pools returned by the scrape pools API.
 *
 * @param scrapePools names of all configured scrape pools
 * @author Daehwan Baek
 */
public record ScrapePoolsData(
        List<String> scrapePools
) {

}
