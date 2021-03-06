package io.webby.util.time;

import org.jetbrains.annotations.NotNull;

import java.time.Instant;

// Max supported date: Wed May 30 2514
public class NanoTimestamp {
    public static long instantToLongNano(@NotNull Instant instant) {
        long epochSecond = instant.getEpochSecond();
        int nano = instant.getNano();
        return (epochSecond << 30) + nano;
    }

    public static @NotNull Instant longNanoToInstant(long timestamp) {
        long epochSecond = timestamp >>> 30;
        int nano = (int) (timestamp & 0x3fff_ffffL);  // 0b0011_1111_1111_1111_1111_1111_1111_1111L, 30 bits
        return Instant.ofEpochSecond(epochSecond, nano);
    }

    public static long longNanoToEpochSeconds(long timestamp) {
        return timestamp >>> 30;
    }

    public static long now() {
        return instantToLongNano(Instant.now());
    }
}
