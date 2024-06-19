package io.spbx.webby.perf.stats;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface RenderingStatsListener {
    void report(@NotNull Stat stat, int size, long elapsedMillis, @Nullable Object hint);
}
