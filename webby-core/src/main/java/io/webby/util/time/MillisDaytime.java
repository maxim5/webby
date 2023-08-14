package io.webby.util.time;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.concurrent.TimeUnit;

public class MillisDaytime {
    public static int localDateTimeToMillis(@NotNull LocalDateTime localDateTime) {
        return localTimeToMillis(localDateTime.toLocalTime());
    }

    public static int localTimeToMillis(@NotNull LocalTime localTime) {
        long nanoOfDay = localTime.toNanoOfDay();
        return (int) (nanoOfDay / 1_000_000);
    }

    public static @NotNull LocalTime millisToLocalTime(int micros) {
        long nanoOfDay = micros * 1_000_000L;
        return LocalTime.ofNanoOfDay(nanoOfDay);
    }

    public static int now() {
        return localTimeToMillis(LocalTime.now());
    }

    public static void main(String[] args) {
        long micros = TimeUnit.DAYS.toSeconds(1);
        System.out.println(micros);
        System.out.println(Short.MAX_VALUE);
    }
}
