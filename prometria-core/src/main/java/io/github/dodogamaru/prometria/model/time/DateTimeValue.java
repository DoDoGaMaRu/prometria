package io.github.dodogamaru.prometria.model.time;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Wraps a {@link LocalDateTime} and renders it as epoch seconds.
 *
 * <p>The conversion uses the system default time zone, so the rendered value
 * depends on the time zone of the machine running the application.
 *
 * @author Daehwan Baek
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DateTimeValue {

    private LocalDateTime datetime;

    /**
     * Creates a value wrapping the given time.
     *
     * @param datetime time to wrap
     * @return a new {@code DateTimeValue}
     */
    public static DateTimeValue of(LocalDateTime datetime) {
        return new DateTimeValue(datetime);
    }

    /**
     * Renders the wrapped time as epoch seconds.
     *
     * @return epoch seconds of the wrapped time in the system default time zone
     */
    @Override
    public String toString() {
        ZoneId zoneId = ZoneId.systemDefault();
        Instant instant = datetime.atZone(zoneId).toInstant();
        long epochSecond = instant.atZone(zoneId).toEpochSecond();
        return Long.toString(epochSecond);
    }
}
