package io.webby.netty.dispatch;

import com.google.inject.AbstractModule;
import io.webby.netty.dispatch.http.NettyHttpHandler;

public class DispatchModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(NettyConst.class).asEagerSingleton();
        bind(NettyBootstrap.class).asEagerSingleton();
        bind(NettyDispatcher.class);   // not a singleton!
        bind(NettyHttpHandler.class);  // not a singleton!
    }
}
