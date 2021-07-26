package io.webby.perf;

import com.google.common.flogger.FluentLogger;
import io.netty.handler.codec.http.FullHttpResponse;
import io.webby.netty.intercept.Interceptor;
import io.webby.netty.intercept.attr.Attribute;
import io.webby.netty.intercept.attr.AttributeOwner;
import io.webby.netty.request.HttpRequestEx;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

@AttributeOwner(position = Attribute.Stats)
public class StatsInterceptor implements Interceptor {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    @Override
    public void enter(@NotNull HttpRequestEx request) {
        log.at(Level.INFO).log("Before %s", request.uri());
    }

    @Override
    public @NotNull FullHttpResponse exit(@NotNull HttpRequestEx request, @NotNull FullHttpResponse response) {
        log.at(Level.INFO).log("After  %s: %s", request.uri(), response.status());
        return response;
    }
}
