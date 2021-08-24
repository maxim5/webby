package io.webby.perf;

import com.google.common.flogger.FluentLogger;
import com.google.inject.Inject;
import io.netty.handler.codec.http.HttpResponse;
import io.webby.app.Settings;
import io.webby.netty.intercept.Interceptor;
import io.webby.netty.intercept.attr.AttributeOwner;
import io.webby.netty.intercept.attr.Attributes;
import io.webby.netty.request.MutableHttpRequestEx;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import static io.webby.perf.LocalStatsHolder.*;

@AttributeOwner(position = Attributes.Stats)
public class StatsInterceptor implements Interceptor {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    @Inject private Settings settings;

    @Override
    public boolean isEnabled() {
        return settings.isDevMode();
    }

    @Override
    public void enter(@NotNull MutableHttpRequestEx request) {
        RequestStatsCollector stats = RequestStatsCollector.create(request);
        request.setAttr(Attributes.Stats, stats);

        RequestStatsCollector existing = localStats.get();
        if (existing != null) {
            log.at(Level.WARNING).log("This thread contains a dirty local-stats: id=%x url=%s",
                                      existing.id(), existing.request().uri());
        }
        log.at(Level.FINE).log("Setting a local-stats: id=%x url=%s", stats.id(), request.uri());
        localStats.set(stats);
    }

    @Override
    public @NotNull HttpResponse exit(@NotNull MutableHttpRequestEx request, @NotNull HttpResponse response) {
        RequestStatsCollector stats = request.attrOrDie(Attributes.Stats);
        RequestStatsCollector collector = localStats.get();
        if (collector == null) {
            log.at(Level.WARNING).log("This thread lost a local-stats for url=%s", request.uri());
        } else if (stats.id() != collector.id()) {
            log.at(Level.WARNING).log("This thread contain the wrong local-stats: %s vs %s", stats, collector);
        } else {
            log.at(Level.FINE).log("Cleaning up a local-stats: id=%x url=%s", collector.id(), request.uri());
        }
        localStats.remove();

        log.at(Level.FINE).log("Handler time for %s: %d ms", request.uri(), stats.stopwatch().elapsed(TimeUnit.MILLISECONDS));
        return response;
    }

    public void cleanup() {
        RequestStatsCollector collector = localStats.get();
        if (collector != null) {
            log.at(Level.WARNING).log("Cleaning up a local-stats: id=%x", collector.id());
            localStats.remove();
        }
    }
}
