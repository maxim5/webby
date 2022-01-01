package io.webby.util.time;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static io.webby.util.time.MicroTimestamp.instantToLongMicro;
import static io.webby.util.time.MicroTimestamp.longMicroToInstant;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class MicroTimestampTest {
    @Test
    public void instant_roundtrip() {
        assertInstantRoundtrip(Instant.parse("2021-12-27T17:59:15.566443000Z"));
        assertInstantRoundtrip(Instant.parse("2021-12-27T17:59:15.000000000Z"));
        assertInstantRoundtrip(Instant.parse("2021-12-27T17:59:15.999999000Z"));

        assertInstantRoundtrip(Instant.parse("1970-01-01T00:00:00.000000000Z"));
        assertInstantRoundtrip(Instant.parse("1970-01-09T17:58:38.071544000Z"));
        assertInstantRoundtrip(Instant.parse("2000-12-15T17:59:15.566443000Z"));
        assertInstantRoundtrip(Instant.parse("5000-12-08T18:01:03.533116000Z"));
        assertInstantRoundtrip(Instant.parse("9999-12-08T18:01:03.533116000Z"));
    }

    private static void assertInstantRoundtrip(@NotNull Instant instant) {
        long timestamp = instantToLongMicro(instant);
        Instant back = longMicroToInstant(timestamp);
        assertEquals(instant, back);
    }
}
