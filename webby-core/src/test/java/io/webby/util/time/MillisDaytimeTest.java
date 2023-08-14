package io.webby.util.time;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

import static com.google.common.truth.Truth.assertThat;


public class MillisDaytimeTest {
    @Test
    public void localTime_roundtrip() {
        assertLocalTimeRoundtrip(LocalTime.of(0, 0, 0, 0));
        assertLocalTimeRoundtrip(LocalTime.of(0, 0, 0, 999_999_999));
        assertLocalTimeRoundtrip(LocalTime.of(11, 59, 59, 999_999_999));
        assertLocalTimeRoundtrip(LocalTime.of(12, 30, 15, 777_777_777));
        assertLocalTimeRoundtrip(LocalTime.of(18, 0, 0, 0));
        assertLocalTimeRoundtrip(LocalTime.of(23, 59, 59, 999_999_999));
    }

    private static void assertLocalTimeRoundtrip(@NotNull LocalTime localTime) {
        localTime = localTime.truncatedTo(ChronoUnit.MILLIS);
        int timestamp = MillisDaytime.localTimeToMillis(localTime);
        LocalTime back = MillisDaytime.millisToLocalTime(timestamp);
        assertThat(back).isEqualTo(localTime);
    }
}
