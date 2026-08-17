package io.github.dodogamaru.prometria.model.time;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DateTimeValueTest {

    @Test
    void toStringReturnsEpochSecondsInSystemDefaultZone() {
        LocalDateTime time = LocalDateTime.of(2026, 8, 15, 12, 0, 0);
        long expected = time.atZone(ZoneId.systemDefault()).toEpochSecond();

        assertEquals(Long.toString(expected), DateTimeValue.of(time).toString());
    }
}
