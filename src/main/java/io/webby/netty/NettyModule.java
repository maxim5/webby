package io.webby.netty;

import com.google.inject.AbstractModule;
import io.webby.netty.intercept.InterceptorScanner;
import io.webby.netty.intercept.Interceptors;
import io.webby.netty.marshal.MarshallerFactory;
import io.webby.netty.response.HttpResponseFactory;
import io.webby.netty.response.ResponseMapper;
import io.webby.netty.response.StaticServing;
import io.webby.netty.ws.ChannelMessageSender;
import io.webby.netty.ws.ChannelSender;
import io.webby.ws.MessageSender;
import io.webby.ws.Sender;

public class NettyModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(HttpResponseFactory.class).asEagerSingleton();
        bind(ResponseMapper.class).asEagerSingleton();
        bind(StaticServing.class).asEagerSingleton();

        bind(Sender.class).to(ChannelSender.class);  // not a singleton!
        bind(MessageSender.class).to(ChannelMessageSender.class);  // not a singleton!

        bind(Interceptors.class).asEagerSingleton();
        bind(InterceptorScanner.class).asEagerSingleton();

        bind(MarshallerFactory.class).asEagerSingleton();

        bind(NettyBootstrap.class).asEagerSingleton();
        bind(NettyDispatcher.class);   // not a singleton!
        bind(NettyHttpHandler.class);  // not a singleton!
    }
}
