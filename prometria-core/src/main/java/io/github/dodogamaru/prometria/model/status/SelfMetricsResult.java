package io.github.dodogamaru.prometria.model.status;

import java.util.List;

/**
 * Response of the self metrics API ({@code /status/self_metrics}).
 *
 * @param status query status ({@code success} or {@code error})
 * @param data   the Prometheus process's own metrics as protobuf-JSON metric families
 * @author Daehwan Baek
 */
public record SelfMetricsResult(
        String status,
        List<SelfMetricsFamily> data
) {

}
