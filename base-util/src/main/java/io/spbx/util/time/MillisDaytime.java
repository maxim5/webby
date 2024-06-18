package io.spbx.util.time;

import org.checkerframework.dataflow.qual.Pure;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.LocalTime;

public class MillisDaytime {
    @Pure
    public static int localDateTimeToMillis(@NotNull LocalDateTime localDateTime) {
        return localTimeToMillis(localDateTime.toLocalTime());
    }

    @Pure
    public static int localTimeToMillis(@NotNull LocalTime localTime) {
        long nanoOfDay = localTime.toNanoOfDay();
        return (int) (nanoOfDay / 1_000_000);
    }

    @Pure
    public static @NotNull LocalTime millisToLocalTime(int micros) {
        long nanoOfDay = micros * 1_000_000L;
        return LocalTime.ofNanoOfDay(nanoOfDay);
    }

    @Pure
    public static int now() {
        return localTimeToMillis(LocalTime.now());
    }
}
