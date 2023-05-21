package io.webby.perf.stats.impl;

import org.jetbrains.annotations.NotNull;

public class LocalStatsHolder {
    static final ThreadLocal<StatsCollector> localStatsRef = new ThreadLocal<>();

    private static final StatsCollector EMPTY_COLLECTOR = new StatsCollector(0) {
        @Override
        public boolean lock() {
            return false;
        }
    };

    public static @NotNull StatsCollector getLocalStats() {
        StatsCollector stats = localStatsRef.get();
        return stats != null ? stats : EMPTY_COLLECTOR;
    }
}
