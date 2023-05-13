package io.webby.netty;

import com.google.inject.AbstractModule;
import io.webby.netty.dispatch.DispatchModule;
import io.webby.netty.intercept.InterceptModule;
import io.webby.netty.marshal.MarshalModule;
import io.webby.netty.request.RequestModule;
import io.webby.netty.response.ResponseModule;
import io.webby.netty.ws.FrameModule;

public class NettyModule extends AbstractModule {
    @Override
    protected void configure() {
        install(new RequestModule());
        install(new ResponseModule());
        install(new InterceptModule());
        install(new MarshalModule());
        install(new DispatchModule());
        install(new FrameModule());
    }
}
