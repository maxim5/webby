package io.webby.ws.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.flogger.FluentLogger;
import com.google.inject.Inject;
import io.webby.util.time.TimeIt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.Map;
import java.util.logging.Level;

public class WebsocketRouter {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    private final ImmutableMap<String, AgentEndpoint> router;

    @Inject
    public WebsocketRouter(@NotNull WebsocketAgentBinder agentBinder) {
        router = TimeIt.timeIt(() -> {
            Map<String, AgentEndpoint> boundAgents = agentBinder.bindAgents();
            return ImmutableMap.copyOf(boundAgents);
        }).onDone((__, millis) -> log.at(Level.INFO).log("Websocket router built in %d ms", millis));
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
            .filter(endpoint -> klass.isInstance(endpoint.agent()))
            .findAny()
            .orElse(null);
    }
}
