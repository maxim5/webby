package io.webby.util.time;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static com.google.common.truth.Truth.assertThat;

public class NanoTimestampTest {
    @Test
    public void instant_roundtrip() {
        assertInstantRoundtrip(Instant.parse("2021-12-27T17:59:15.566443700Z"));
        assertInstantRoundtrip(Instant.parse("2021-12-27T17:59:15.000000000Z"));
        assertInstantRoundtrip(Instant.parse("2021-12-27T17:59:15.999999999Z"));

        assertInstantRoundtrip(Instant.parse("1970-01-01T00:00:00.000000000Z"));
        assertInstantRoundtrip(Instant.parse("1970-01-09T17:58:38.071544600Z"));
        assertInstantRoundtrip(Instant.parse("2070-12-15T17:59:15.566443700Z"));
        assertInstantRoundtrip(Instant.parse("2100-12-08T18:01:03.533116200Z"));
        assertInstantRoundtrip(Instant.parse("2200-12-08T18:01:03.533116200Z"));
        assertInstantRoundtrip(Instant.parse("2240-12-08T18:01:03.533116200Z"));
        assertInstantRoundtrip(Instant.parse("2510-12-08T18:01:03.533116200Z"));
    }

    private static void assertInstantRoundtrip(@NotNull Instant instant) {
        long timestamp = NanoTimestamp.instantToLongNano(instant);
        Instant back = NanoTimestamp.longNanoToInstant(timestamp);
        assertThat(back).isEqualTo(instant);
    }
}
