package io.github.dodogamaru.prometria.model.targets;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * A relabel rule as configured for a scrape pool. All fields are optional in
 * the JSON ({@code omitempty}), so absent values map to {@code null}.
 *
 * @param sourceLabels label names whose values are concatenated and matched
 * @param separator    separator between concatenated values
 * @param regex        regular expression (already unescaped)
 * @param modulus      hash modulus for the {@code hashmod} action
 * @param targetLabel  label the result is written to
 * @param replacement  replacement pattern ({@code $1} interpolation)
 * @param action       relabel action: {@code replace}, {@code keep}, {@code drop},
 *                     {@code hashmod}, {@code labelmap}, {@code labeldrop}, {@code labelkeep}
 * @author Daehwan Baek
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record RelabelConfig(
        @JsonProperty("source_labels") List<String> sourceLabels,
        String separator,
        String regex,
        Long modulus,
        @JsonProperty("target_label") String targetLabel,
        String replacement,
        String action
) {

}
