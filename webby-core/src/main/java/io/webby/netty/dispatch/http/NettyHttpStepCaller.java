package io.webby.netty.dispatch.http;

import com.google.common.flogger.FluentLogger;
import com.google.inject.Inject;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.routekit.Match;
import io.webby.netty.errors.ServeException;
import io.webby.netty.response.HttpResponseFactory;
import io.webby.url.caller.Caller;
import io.webby.url.convert.ConversionError;
import io.webby.url.impl.Endpoint;
import io.webby.url.impl.EndpointOptions;
import io.webby.url.impl.RouteEndpoint;
import io.webby.util.func.ThrowBiFunction;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;

class NettyHttpStepCaller implements ChannelContextBound {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    @Inject private HttpResponseFactory responses;

    @Override
    public void bindContext(@NotNull ChannelHandlerContext context) {
    }

    @NotNull HttpResponse callEndpoint(@NotNull FullHttpRequest request,
                                       @NotNull Match<RouteEndpoint> match,
                                       @NotNull Endpoint endpoint,
                                       @NotNull ThrowBiFunction<Object, EndpointOptions, HttpResponse, Throwable> next) {
        Caller caller = endpoint.caller();
        try {
            Object callResult = caller.call(request, match.variables());
            if (callResult == null) {
                if (endpoint.context().isVoid()) {
                    log.at(Level.FINE).log("Request handler is void, transforming into empty string");
                } else {
                    log.at(Level.WARNING).log("Request handler returned null: %s", caller.method());
                }
                return next.apply("", endpoint.options());
            }
            return next.apply(callResult, endpoint.options());
        } catch (Throwable e) {
            return callException(e, caller);
        }
    }

    private @NotNull HttpResponse callException(@NotNull Throwable error, @NotNull Caller caller) {
        if (error instanceof ConversionError) {
            log.at(Level.INFO).withCause(error).log("Request validation failed: %s", error.getMessage());
            return responses.newResponse400(error);
        }
        if (error instanceof ServeException e) {
            return responses.handleServeException(e, "Request handler %s".formatted(caller.method()));
        }
        if (error instanceof InvocationTargetException) {
            return callException(error.getCause(), caller);
        }

        log.at(Level.SEVERE).withCause(error).log("Failed to call method: %s", caller.method());
        return responses.newResponse500("Failed to call method: %s".formatted(caller.method()), error);
    }
}
