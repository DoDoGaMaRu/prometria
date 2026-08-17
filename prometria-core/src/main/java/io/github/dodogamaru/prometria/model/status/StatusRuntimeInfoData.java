package io.github.dodogamaru.prometria.model.status;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Runtime information properties of the Prometheus server
 * ({@code /status/runtimeinfo}).
 *
 * <p>The exact set of properties may change without notice between
 * Prometheus versions; unknown properties are ignored and absent ones
 * are {@code null}.
 *
 * @param startTime           RFC3339 timestamp when the server was started
 * @param cwd                 current working directory of the server process
 * @param hostname            hostname of the server process
 * @param serverTime          current server time (RFC3339)
 * @param reloadConfigSuccess whether the last configuration reload succeeded
 * @param lastConfigTime      RFC3339 timestamp of the last configuration load
 * @param timeSeriesCount     number of time series in the storage (newer versions)
 * @param corruptionCount     number of detected storage corruptions
 * @param goroutineCount      number of active Go goroutines
 * @param gomaxprocs          GOMAXPROCS setting
 * @param goMemLimit          GOMEMLIMIT in bytes
 * @param gogc                GOGC setting
 * @param godebug             GODEBUG setting
 * @param storageRetention    configured storage retention
 * @author Daehwan Baek
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record StatusRuntimeInfoData(
        String startTime,
        @JsonProperty("CWD") String cwd,
        String hostname,
        String serverTime,
        boolean reloadConfigSuccess,
        String lastConfigTime,
        Long timeSeriesCount,
        long corruptionCount,
        int goroutineCount,
        @JsonProperty("GOMAXPROCS") int gomaxprocs,
        @JsonProperty("GOMEMLIMIT") Long goMemLimit,
        @JsonProperty("GOGC") String gogc,
        @JsonProperty("GODEBUG") String godebug,
        String storageRetention
) {

}
