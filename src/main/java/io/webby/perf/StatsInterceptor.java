package io.webby.perf;

import com.google.common.flogger.FluentLogger;
import io.netty.handler.codec.http.FullHttpResponse;
import io.webby.netty.intercept.Interceptor;
import io.webby.netty.intercept.attr.Attributes;
import io.webby.netty.intercept.attr.AttributeOwner;
import io.webby.netty.request.MutableHttpRequestEx;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

@AttributeOwner(position = Attributes.Stats)
public class StatsInterceptor implements Interceptor {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    @Override
    public void enter(@NotNull MutableHttpRequestEx request) {
        request.setAttr(Attributes.Stats, StatsTracker.create());
    }

    @Override
    public @NotNull FullHttpResponse exit(@NotNull MutableHttpRequestEx request, @NotNull FullHttpResponse response) {
        StatsTracker stats = request.attrOrDie(Attributes.Stats);
        log.at(Level.FINE).log("Handler time for %s: %d ms", request.uri(), stats.stopwatch().elapsed(TimeUnit.MILLISECONDS));
        return response;
    }
}
