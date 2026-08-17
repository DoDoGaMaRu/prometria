package io.github.dodogamaru.prometria.model.rules;

import java.util.List;

/**
 * Active alerts returned by the alerts API.
 *
 * @param alerts all currently active alerts
 * @author Daehwan Baek
 */
public record AlertsData(
        List<Alert> alerts
) {

}
