package io.webby.url.impl;

import com.google.common.flogger.FluentLogger;
import com.google.inject.Inject;
import com.google.inject.Provider;
import io.routekit.Router;
import io.webby.util.base.TimeIt;
import io.webby.ws.impl.WebsocketAgentBinder;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

public class UrlRouter implements Provider<Router<RouteEndpoint>> {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    private final Router<RouteEndpoint> router;

    @Inject
    public UrlRouter(@NotNull HandlerBinder handlerBinder, @NotNull WebsocketAgentBinder agentBinder) {
        this.router = TimeIt.timeIt(
            handlerBinder::buildHandlerRouter,
            (result, millis) -> log.at(Level.INFO).log("URL router built in %d ms", millis)
        );
    }

    @Override
    public @NotNull Router<RouteEndpoint> get() {
        return router;
    }
}
