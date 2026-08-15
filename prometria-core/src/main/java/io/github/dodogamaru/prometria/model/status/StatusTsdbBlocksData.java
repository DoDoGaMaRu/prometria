package io.github.dodogamaru.prometria.model.status;

import java.util.List;

/**
 * Payload of the TSDB blocks API.
 *
 * @param blocks compacted TSDB blocks (the head is not included)
 * @author Daehwan Baek
 */
public record StatusTsdbBlocksData(
        List<TsdbBlock> blocks
) {

}
