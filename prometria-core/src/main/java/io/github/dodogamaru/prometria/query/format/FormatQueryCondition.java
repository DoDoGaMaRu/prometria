package io.github.dodogamaru.prometria.query.format;

import io.github.dodogamaru.prometria.query.Condition;

/**
 * Condition for the format query API ({@code /api/v1/format_query}).
 *
 * <p>The expression to format is the method's {@code @PromQL} template, not a
 * query parameter of a condition; the API takes no other query parameters.
 * The condition exists so the operation can be routed like every other one.
 *
 * @author Daehwan Baek
 */
public class FormatQueryCondition implements Condition {

}
