package io.github.dodogamaru.prometria.model.discovery;

/**
 * Response of the scrape pools API ({@code /scrape_pools}).
 *
 * @param status query status ({@code success} or {@code error})
 * @param data   the configured scrape pools
 * @author Daehwan Baek
 */
public record ScrapePoolsResult(
        String status,
        ScrapePoolsData data
) {

}
