package io.github.dodogamaru.prometria.model.rules;

import java.util.List;

/**
 * Alertmanager discovery state returned by the alertmanagers API.
 *
 * @param activeAlertmanagers  Alertmanager endpoints currently used
 * @param droppedAlertmanagers Alertmanager endpoints dropped during discovery
 * @author Daehwan Baek
 */
public record AlertmanagersData(
        List<AlertManager> activeAlertmanagers,
        List<AlertManager> droppedAlertmanagers
) {

}
