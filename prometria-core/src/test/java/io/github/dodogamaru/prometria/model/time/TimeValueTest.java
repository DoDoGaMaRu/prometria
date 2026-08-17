package io.github.dodogamaru.prometria.model.time;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TimeValueTest {

    @Test
    void toStringFormatsTimeWithUnitSuffix() {
        assertEquals("30s", TimeValue.of(30L, TimeUnit.SECONDS).toString());
        assertEquals("2m", TimeValue.of(2L, TimeUnit.MINUTES).toString());
        assertEquals("24h", TimeValue.of(24L, TimeUnit.HOURS).toString());
    }
}
