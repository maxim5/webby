package io.webby.ws.impl;

import com.google.common.base.Stopwatch;
import com.google.common.flogger.FluentLogger;
import com.google.inject.Inject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class WebsocketRouter {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    private final Map<String, AgentEndpoint> router;

    @Inject
    public WebsocketRouter(@NotNull WebsocketAgentBinder agentBinder) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        router = agentBinder.bindAgents();
        log.at(Level.INFO).log("URL router built in %d ms", stopwatch.stop().elapsed(TimeUnit.MILLISECONDS));
    }

    public @Nullable AgentEndpoint route(@NotNull String url) {
        int index = url.indexOf('?');
        String path = (index >= 0) ? url.substring(0, index) : url;
        return router.get(path);
    }

    @VisibleForTesting
    public @Nullable AgentEndpoint findAgentEndpointByClass(@NotNull Class<?> klass) {
        return router.values()
                .stream()
                .filter(endpoint -> klass.isInstance(endpoint.instance()))
                .findAny()
                .orElse(null);
    }
}
