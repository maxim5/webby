package io.webby.netty;

import com.google.inject.AbstractModule;
import io.webby.netty.response.HttpResponseFactory;
import io.webby.netty.response.StaticServing;

public class NettyModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(HttpResponseFactory.class).asEagerSingleton();
        bind(NettyChannelHandler.class);  // not a singleton!
        bind(NettyStartup.class).asEagerSingleton();
        bind(StaticServing.class).asEagerSingleton();
    }
}
