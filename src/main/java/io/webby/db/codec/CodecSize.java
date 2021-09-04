package io.webby.db.codec;

import org.jetbrains.annotations.NotNull;

public record CodecSize(@NotNull Estimate estimate, long numBytes) {
    public enum Estimate {
        FIXED,
        MIN,
        AVERAGE
    }

    public static @NotNull CodecSize fixed(long numBytes) {
        return new CodecSize(Estimate.FIXED, numBytes);
    }

    public static @NotNull CodecSize minSize(long numBytes) {
        return new CodecSize(Estimate.MIN, numBytes);
    }

    public static @NotNull CodecSize averageSize(long numBytes) {
        return new CodecSize(Estimate.AVERAGE, numBytes);
    }

    public boolean isFixed() {
        return estimate == Estimate.FIXED;
    }
}
