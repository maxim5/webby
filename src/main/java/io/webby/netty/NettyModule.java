package io.webby.netty;

import com.google.inject.AbstractModule;

public class NettyModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(HttpResponseFactory.class).asEagerSingleton();
        bind(StaticServing.class).asEagerSingleton();
    }
}
