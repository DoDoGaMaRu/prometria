package io.github.dodogamaru.prometria.model.discovery;

import java.util.List;
import java.util.Map;

/**
 * Response of the series API ({@code /series}).
 *
 * @param status query status ({@code success} or {@code error})
 * @param data   matched series, each as a label set ({@code label name → value}, includes {@code __name__})
 * @author Daehwan Baek
 */
public record SeriesResult(
        String status,
        List<Map<String, String>> data
) {

}
