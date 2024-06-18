package io.spbx.webby.perf.stats.impl;

import org.jetbrains.annotations.Nullable;

public record StatsRecord(long elapsedMillis, @Nullable Object hint) {
}
