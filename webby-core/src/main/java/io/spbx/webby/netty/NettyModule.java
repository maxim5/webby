package io.spbx.webby.netty;

import com.google.inject.AbstractModule;
import io.spbx.webby.netty.dispatch.DispatchModule;
import io.spbx.webby.netty.intercept.InterceptModule;
import io.spbx.webby.netty.marshal.MarshalModule;
import io.spbx.webby.netty.request.RequestModule;
import io.spbx.webby.netty.response.ResponseModule;
import io.spbx.webby.netty.ws.FrameModule;

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
