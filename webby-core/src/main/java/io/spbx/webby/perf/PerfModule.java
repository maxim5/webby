package io.spbx.webby.perf;

import com.google.inject.AbstractModule;
import io.spbx.webby.perf.stats.impl.StatsInterceptor;
import io.spbx.webby.perf.stats.impl.StatsManager;

public class PerfModule extends AbstractModule {
    public void configure() {
        bind(StatsInterceptor.class).asEagerSingleton();
        bind(StatsManager.class).asEagerSingleton();
    }
}
