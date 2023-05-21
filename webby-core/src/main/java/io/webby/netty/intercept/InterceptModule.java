package io.webby.netty.intercept;

import com.google.inject.AbstractModule;

public class InterceptModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(InterceptorScanner.class).asEagerSingleton();
        bind(Interceptors.class).asEagerSingleton();
        bind(InterceptorsStack.class).to(Interceptors.class).asEagerSingleton();
    }
}
