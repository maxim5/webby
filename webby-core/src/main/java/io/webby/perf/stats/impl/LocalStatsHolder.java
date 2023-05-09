package io.webby.perf.stats.impl;

import org.jetbrains.annotations.NotNull;

public class LocalStatsHolder {
    static final ThreadLocal<StatsCollector> localStats = new ThreadLocal<>();

    public static @NotNull StatsCollector getLocalStats() {
        StatsCollector stats = localStats.get();
        return stats != null ? stats : StatsCollector.EMPTY;
    }
}
