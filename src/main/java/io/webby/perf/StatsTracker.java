package io.webby.perf;

import com.google.common.base.Stopwatch;
import org.jetbrains.annotations.NotNull;

public record StatsTracker(@NotNull Stopwatch stopwatch) {
    public static StatsTracker create() {
        return new StatsTracker(Stopwatch.createStarted());
    }
}
