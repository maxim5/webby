package io.webby.perf.stats.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record StatsRecord(long elapsedMillis, @Nullable Object hint) {
    public @NotNull String toCompactString() {
        return hint != null ?
                "[%d,'%s']".formatted(elapsedMillis, hint.toString()) :
                "[%d]".formatted(elapsedMillis);
    }
}
