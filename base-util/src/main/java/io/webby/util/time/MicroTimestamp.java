package io.webby.util.time;

import org.checkerframework.dataflow.qual.Pure;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;

public class MicroTimestamp {
    @Pure
    public static long instantToLongMicro(@NotNull Instant instant) {
        long epochSecond = instant.getEpochSecond();
        int nano = instant.getNano();
        return (epochSecond << 20) + (nano / 1000);
    }

    @Pure
    public static @NotNull Instant longMicroToInstant(long timestamp) {
        long epochSecond = timestamp >>> 20;
        int micro = (int) (timestamp & 0x000f_ffffL);  // 0b1111_1111_1111_1111_1111L, 20 bits
        return Instant.ofEpochSecond(epochSecond, micro * 1000);
    }

    @Pure
    public static long longMicroToEpochSeconds(long timestamp) {
        return timestamp >>> 20;
    }

    @Pure
    public static long now() {
        return instantToLongMicro(Instant.now());
    }
}
