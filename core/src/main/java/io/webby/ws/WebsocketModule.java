package io.webby.ws;

import com.google.inject.AbstractModule;
import io.webby.ws.impl.WebsocketAgentBinder;
import io.webby.ws.impl.WebsocketAgentScanner;
import io.webby.ws.impl.WebsocketRouter;

public class WebsocketModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(WebsocketAgentBinder.class).asEagerSingleton();
        bind(WebsocketAgentScanner.class).asEagerSingleton();
        bind(WebsocketRouter.class).asEagerSingleton();
    }
}
