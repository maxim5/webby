package io.webby.perf;

import org.jetbrains.annotations.Nullable;

public record StatsRecord(long elapsedMillis, @Nullable Object hint) {
}
