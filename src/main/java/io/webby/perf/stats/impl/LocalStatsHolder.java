package io.webby.perf.stats.impl;

import org.jetbrains.annotations.NotNull;

public class LocalStatsHolder {
    static final ThreadLocal<RequestStatsCollector> localStats = new ThreadLocal<>();

    public static @NotNull RequestStatsCollector getLocalStats() {
        RequestStatsCollector stats = localStats.get();
        return stats != null ? stats : RequestStatsCollector.EMPTY;
    }
}
