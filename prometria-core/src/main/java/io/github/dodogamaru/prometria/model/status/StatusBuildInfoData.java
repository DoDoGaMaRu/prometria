package io.github.dodogamaru.prometria.model.status;

/**
 * Build information properties of the Prometheus server
 * ({@code /status/buildinfo}).
 *
 * <p>The exact set of properties may change without notice between
 * Prometheus versions.
 *
 * @param version   Prometheus version
 * @param revision  VCS revision the server was built from
 * @param branch    VCS branch the server was built from
 * @param buildUser user that built the server
 * @param buildDate build date ({@code yyyymmdd-hh:mm:ss})
 * @param goVersion Go version the server was built with
 * @author Daehwan Baek
 */
public record StatusBuildInfoData(
        String version,
        String revision,
        String branch,
        String buildUser,
        String buildDate,
        String goVersion
) {

}
