package io.webby.perf;

import com.google.inject.AbstractModule;

public class PerfModule extends AbstractModule {
    public void configure() {
        bind(StatsManager.class).asEagerSingleton();
        bind(StatsInterceptor.class).asEagerSingleton();
    }
}
