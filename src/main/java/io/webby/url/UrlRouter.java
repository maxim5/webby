package io.webby.url;

import com.google.common.base.Stopwatch;
import com.google.common.flogger.FluentLogger;
import com.google.inject.Inject;
import io.routekit.Router;
import io.webby.url.caller.Caller;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class UrlRouter {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    private final Router<Caller> router;

    @Inject
    public UrlRouter(@NotNull UrlBinder binder) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        this.router = binder.bindRouter();
        log.at(Level.INFO).log("URL router built in %d ms", stopwatch.stop().elapsed(TimeUnit.MILLISECONDS));
    }

    @NotNull
    public Router<Caller> getRouter() {
        return router;
    }
}
