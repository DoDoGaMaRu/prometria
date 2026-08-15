package io.github.dodogamaru.prometria.model.status;

import java.util.List;

/**
 * Compaction provenance of a TSDB block.
 *
 * @param level     compaction level the block was created at
 * @param sources   ULIDs of the source head blocks, if any
 * @param deletable whether the block is empty and should be deleted
 * @param parents   direct blocks that were used to create this block
 * @param failed    whether the compaction failed
 * @param hints     additional compaction hints
 * @author Daehwan Baek
 */
public record TsdbBlockCompaction(
        Integer level,
        List<String> sources,
        Boolean deletable,
        List<TsdbBlockDesc> parents,
        Boolean failed,
        List<String> hints
) {

}
