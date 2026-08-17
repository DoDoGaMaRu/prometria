package io.github.dodogamaru.prometria.model.discovery;

import java.util.List;

/**
 * Response of the label names API ({@code /labels}).
 *
 * @param status query status ({@code success} or {@code error})
 * @param data   label names (sorted); includes {@code __name__}
 * @author Daehwan Baek
 */
public record LabelNamesResult(
        String status,
        List<String> data
) {

}
