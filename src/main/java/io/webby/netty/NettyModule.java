package io.webby.netty;

import com.google.inject.AbstractModule;
import io.webby.netty.intercept.InterceptorScanner;
import io.webby.netty.intercept.Interceptors;
import io.webby.netty.response.HttpResponseFactory;
import io.webby.netty.response.ResponseMapper;
import io.webby.netty.response.StaticServing;

public class NettyModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(NettyBootstrap.class).asEagerSingleton();

        bind(HttpResponseFactory.class).asEagerSingleton();
        bind(ResponseMapper.class).asEagerSingleton();
        bind(StaticServing.class).asEagerSingleton();

        bind(Interceptors.class).asEagerSingleton();
        bind(InterceptorScanner.class).asEagerSingleton();

        bind(NettyChannelHandler.class);  // not a singleton!
    }
}
