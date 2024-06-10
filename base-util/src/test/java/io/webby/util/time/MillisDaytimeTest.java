package io.webby.util.time;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static com.google.common.truth.Truth.assertThat;


public class MillisDaytimeTest {
    @Test
    public void localTime_roundtrip() {
        assertLocalTimeRoundtrip(LocalTime.of(0, 0, 0, 0));
        assertLocalTimeRoundtrip(LocalTime.of(0, 0, 0, 999_000_000));
        assertLocalTimeRoundtrip(LocalTime.of(11, 59, 59, 999_000_000));
        assertLocalTimeRoundtrip(LocalTime.of(12, 30, 15, 777_000_000));
        assertLocalTimeRoundtrip(LocalTime.of(18, 0, 0, 0));
        assertLocalTimeRoundtrip(LocalTime.of(23, 59, 59, 999_000_000));
    }

    private static void assertLocalTimeRoundtrip(@NotNull LocalTime localTime) {
        int timestamp = MillisDaytime.localTimeToMillis(localTime);
        LocalTime back = MillisDaytime.millisToLocalTime(timestamp);
        assertThat(back).isEqualTo(localTime);
    }
}
