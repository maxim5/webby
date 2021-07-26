package io.webby.netty.intercept;

import com.google.common.collect.Lists;
import com.google.common.flogger.FluentLogger;
import com.google.inject.Inject;
import io.netty.handler.codec.http.FullHttpResponse;
import io.webby.netty.exceptions.BadRequestException;
import io.webby.netty.exceptions.NotFoundException;
import io.webby.netty.exceptions.RedirectException;
import io.webby.netty.request.HttpRequestEx;
import io.webby.netty.response.HttpResponseFactory;
import io.webby.perf.StatsInterceptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class Interceptors {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    private final ArrayList<Interceptor> stack = new ArrayList<>(List.of(new StatsInterceptor()));

    @Inject private HttpResponseFactory factory;

    @Nullable
    public FullHttpResponse enter(@NotNull HttpRequestEx request) {
        for (Interceptor interceptor : stack) {
            try {
                interceptor.enter(request);
            } catch (BadRequestException e) {
                log.at(Level.WARNING).withCause(e).log("Interceptor %s raised BAD_REQUEST: %s", interceptor, e.getMessage());
                return factory.newResponse400(e);
            } catch (NotFoundException e) {
                log.at(Level.WARNING).withCause(e).log("Interceptor %s raised NOT_FOUND: %s", interceptor, e.getMessage());
                return factory.newResponse404(e);
            } catch (RedirectException e) {
                log.at(Level.WARNING).withCause(e).log("Interceptor %s raised REDIRECT: %s (%s): %s",
                        interceptor, e.uri(), e.isPermanent() ? "permanent" : "temporary", e.getMessage());
                return factory.newResponseRedirect(e.uri(), e.isPermanent());
            }
        }
        return null;
    }

    @NotNull
    public FullHttpResponse exit(@NotNull HttpRequestEx request, @NotNull FullHttpResponse response) {
        for (Interceptor interceptor : Lists.reverse(stack)) {
            response = interceptor.exit(request, response);
        }
        return response;
    }
}
