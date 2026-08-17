package io.github.dodogamaru.prometria.client;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The query parameters of a request to the Prometheus HTTP API.
 *
 * <p>Assembled fluently by the query handlers and the direct-use API
 * classes:
 * <pre>{@code
 * QueryParams params = QueryParams.create()
 *         .addIfPresent("time", condition.getTime())
 *         .addIfPresent("timeout", condition.getTimeout());
 * }</pre>
 *
 * @author Daehwan Baek
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class QueryParams {

    private final Map<String, List<String>> values = new LinkedHashMap<>();

    /**
     * @return a new, empty parameter set
     */
    public static QueryParams create() {
        return new QueryParams();
    }

    /**
     * URL-encodes a value using UTF-8.
     *
     * @param value value to encode
     * @return the percent-encoded value
     */
    public static String encode(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Adds a query parameter. The same name may be added repeatedly to build
     * a repeated parameter.
     *
     * @param name  parameter name (e.g. {@code match[]})
     * @param value parameter value, rendered with {@link Object#toString()}
     * @return this instance
     */
    public QueryParams add(String name, Object value) {
        values.computeIfAbsent(name, k -> new ArrayList<>()).add(value.toString());
        return this;
    }

    /**
     * Adds a query parameter only if the value is not {@code null}.
     *
     * @param name  parameter name
     * @param value parameter value, rendered with {@link Object#toString()}
     * @return this instance
     */
    public QueryParams addIfPresent(String name, Object value) {
        if (value != null) {
            add(name, value);
        }
        return this;
    }

    /**
     * Adds a repeated query parameter for every element of the list.
     *
     * @param name   parameter name (e.g. {@code match[]})
     * @param values values to add; ignored when {@code null}
     * @return this instance
     */
    public QueryParams addAll(String name, List<String> values) {
        if (values != null) {
            values.forEach(value -> add(name, value));
        }
        return this;
    }

    /**
     * @return the parameters as an unmodifiable map from name to ordered values
     */
    public Map<String, List<String>> toMap() {
        return Collections.unmodifiableMap(values);
    }
}
