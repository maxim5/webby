package io.webby.url.impl;

import com.google.common.base.Stopwatch;
import com.google.common.flogger.FluentLogger;
import com.google.inject.Inject;
import com.google.inject.Provider;
import io.routekit.Router;
import io.webby.url.ws.AgentEndpoint;
import io.webby.url.ws.WebsocketAgentBinder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class UrlRouter implements Provider<Router<RouteEndpoint>> {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    private final Router<RouteEndpoint> router;
    private final Map<String, AgentEndpoint> websocketRouter;

    @Inject
    public UrlRouter(@NotNull HandlerBinder handlerBinder, @NotNull WebsocketAgentBinder agentBinder) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        this.router = handlerBinder.buildHandlerRouter();
        log.at(Level.INFO).log("URL router built in %d ms", stopwatch.stop().elapsed(TimeUnit.MILLISECONDS));

        websocketRouter = agentBinder.bindAgents();
    }

    @Override
    public @NotNull Router<RouteEndpoint> get() {
        return router;
    }

    public @Nullable AgentEndpoint routeWebSocket(@NotNull String url) {
        return websocketRouter.get(url);
    }

    @VisibleForTesting
    public @Nullable AgentEndpoint findAgentEndpointByClass(@NotNull Class<?> klass) {
        return websocketRouter.values()
                .stream()
                .filter(endpoint -> klass.isInstance(endpoint.instance()))
                .findAny()
                .orElse(null);
    }
}
