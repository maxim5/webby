package io.webby.perf;

import com.google.inject.AbstractModule;

public class PerfModule extends AbstractModule {
    public void configure() {
        bind(StatsInterceptor.class).asEagerSingleton();
    }
}
