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
            log.at(Level.WARNING).log("This thread contains a dirty local-stats: %s", existing);
        }
        log.at(Level.FINE).log("Setting a local-stats: %s", stats);
        localStats.set(stats);
    }

    @Override
    public @NotNull HttpResponse exit(@NotNull MutableHttpRequestEx request, @NotNull HttpResponse response) {
        RequestStatsCollector stats = request.attrOrDie(Attributes.Stats);
        RequestStatsCollector local = localStats.get();
        if (local == null) {
            log.at(Level.WARNING).log("This thread lost a local-stats for url=%s", request.uri());
        } else if (stats.id() != local.id()) {
            log.at(Level.WARNING).log("This thread contain the wrong local-stats: %s vs %s", stats, local);
        } else {
            log.at(Level.FINE).log("Cleaning up a local-stats: %s", local);
        }
        localStats.remove();

        log.at(Level.FINE).log("Handle time for %s: %d ms", request.uri(), stats.totalElapsed(TimeUnit.MILLISECONDS));
        return response;
    }

    public void cleanup() {
        RequestStatsCollector local = localStats.get();
        if (local != null) {
            log.at(Level.WARNING).log("Cleaning up a local-stats: %s", local);
            localStats.remove();
        }
    }
}
