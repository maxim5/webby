package io.webby.db.codec;

import org.jetbrains.annotations.NotNull;

public record CodecSize(@NotNull Estimate estimate, int numBytes) {
    public enum Estimate {
        FIXED,
        MIN,
        AVERAGE
    }

    public static @NotNull CodecSize fixed(long numBytes) {
        assert numBytes >= 0 && numBytes == (int) numBytes : "Unsupported size: %d".formatted(numBytes);
        return new CodecSize(Estimate.FIXED, (int) numBytes);
    }

    public static @NotNull CodecSize minSize(long numBytes) {
        assert numBytes == (int) numBytes : "Unsupported size: %d".formatted(numBytes);
        return new CodecSize(Estimate.MIN, (int) numBytes);
    }

    public static @NotNull CodecSize averageSize(long numBytes) {
        assert numBytes == (int) numBytes : "Unsupported size: %d".formatted(numBytes);
        return new CodecSize(Estimate.AVERAGE, (int) numBytes);
    }

    public boolean isFixed() {
        return estimate == Estimate.FIXED;
    }
}
