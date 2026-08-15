package io.github.dodogamaru.prometria.model.targets;

import java.util.List;
import java.util.Map;

/**
 * Target discovery state returned by the targets API.
 *
 * @param activeTargets       targets currently being scraped
 * @param droppedTargets      targets dropped during service discovery
 * @param droppedTargetCounts per-scrape-pool count of dropped targets;
 *                            may be {@code null} (e.g. when filtering by state)
 * @author Daehwan Baek
 */
public record TargetsData(
        List<Target> activeTargets,
        List<DroppedTarget> droppedTargets,
        Map<String, Integer> droppedTargetCounts
) {

}
