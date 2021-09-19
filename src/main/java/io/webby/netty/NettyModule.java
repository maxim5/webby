package io.webby.netty;

import com.google.inject.AbstractModule;
import io.webby.netty.intercept.InterceptorScanner;
import io.webby.netty.intercept.Interceptors;
import io.webby.netty.marshal.Json;
import io.webby.netty.marshal.MarshallerFactory;
import io.webby.netty.response.HttpResponseFactory;
import io.webby.netty.response.ResponseHeaders;
import io.webby.netty.response.ResponseMapper;
import io.webby.netty.response.StaticServing;
import io.webby.netty.ws.sender.ChannelSender;
import io.webby.netty.ws.sender.Sender;

public class NettyModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(HttpResponseFactory.class).asEagerSingleton();
        bind(ResponseHeaders.class).asEagerSingleton();
        bind(ResponseMapper.class).asEagerSingleton();
        bind(StaticServing.class).asEagerSingleton();

        bind(Sender.class).to(ChannelSender.class);  // not a singleton!
        // MessageSender.class must use @ImplementedBy to handle generics (not ideal, but works).
        // bind(MessageSender.class).to(ChannelMessageSender.class);

        bind(Interceptors.class).asEagerSingleton();
        bind(InterceptorScanner.class).asEagerSingleton();

        bind(MarshallerFactory.class).asEagerSingleton();
        bind(Json.class).toProvider(MarshallerFactory.class).asEagerSingleton();

        bind(NettyBootstrap.class).asEagerSingleton();
        bind(NettyDispatcher.class);   // not a singleton!
        bind(NettyHttpHandler.class);  // not a singleton!
    }
}
