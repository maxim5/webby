package io.webby.url.impl;

import com.google.common.base.Stopwatch;
import com.google.common.flogger.FluentLogger;
import com.google.inject.Inject;
import com.google.inject.Provider;
import io.routekit.Router;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class UrlRouter implements Provider<Router<RouteEndpoint>> {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    private final Router<RouteEndpoint> router;

    @Inject
    public UrlRouter(@NotNull HandlerBinder binder) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        this.router = binder.buildHandlerRouter();
        log.at(Level.INFO).log("URL router built in %d ms", stopwatch.stop().elapsed(TimeUnit.MILLISECONDS));
    }

    @Override
    public @NotNull Router<RouteEndpoint> get() {
        return router;
    }
}
