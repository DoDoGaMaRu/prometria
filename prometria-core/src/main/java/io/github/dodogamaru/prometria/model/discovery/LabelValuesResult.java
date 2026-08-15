package io.github.dodogamaru.prometria.model.discovery;

import java.util.List;

/**
 * Response of the label values API ({@code /label/{label_name}/values}).
 *
 * @param status query status ({@code success} or {@code error})
 * @param data   label values (sorted)
 * @author Daehwan Baek
 */
public record LabelValuesResult(
        String status,
        List<String> data
) {

}
