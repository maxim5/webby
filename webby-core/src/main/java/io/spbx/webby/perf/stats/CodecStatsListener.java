package io.spbx.webby.perf.stats;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface CodecStatsListener {
    void report(@NotNull Stat stat, int numBytes, long elapsedMillis, @Nullable Object hint);
}
