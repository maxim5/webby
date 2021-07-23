package io.webby.netty;

import com.google.common.base.Stopwatch;
import com.google.common.flogger.FluentLogger;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.inject.Inject;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.routekit.Match;
import io.routekit.Router;
import io.routekit.util.CharBuffer;
import io.webby.netty.exceptions.BadRequestException;
import io.webby.netty.exceptions.NotFoundException;
import io.webby.netty.exceptions.RedirectException;
import io.webby.netty.request.DefaultHttpRequestEx;
import io.webby.netty.response.HttpResponseFactory;
import io.webby.url.annotate.Marshal;
import io.webby.url.caller.Caller;
import io.webby.url.impl.*;
import io.webby.url.convert.ConversionError;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class NettyChannelHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    private final HttpResponseFactory factory;
    private final Router<RouteEndpoint> router;

    @Inject
    public NettyChannelHandler(@NotNull HttpResponseFactory factory, @NotNull UrlRouter urlRouter) {
        this.factory = factory;
        this.router = urlRouter.getRouter();
    }

    @Override
    protected void channelRead0(@NotNull ChannelHandlerContext context, @NotNull FullHttpRequest request) {
        Stopwatch stopwatch = Stopwatch.createStarted();

        FullHttpResponse response;
        try {
            response = handle(request);
        } catch (Throwable throwable) {
            response = factory.newResponse503("Unexpected failure", throwable);
            log.at(Level.SEVERE).withCause(throwable).log("Unexpected failure: %s", throwable.getMessage());
        }
        context.writeAndFlush(response);

        long millis = stopwatch.stop().elapsed(TimeUnit.MILLISECONDS);
        log.at(Level.INFO).log("%s %s: %s (%d ms)", request.method(), request.uri(), response.status(), millis);
    }

    @NotNull
    @VisibleForTesting
    FullHttpResponse handle(@NotNull FullHttpRequest request) {
        CharBuffer uri = new CharBuffer(request.uri());
        Match<RouteEndpoint> match = router.routeOrNull(uri);
        if (match == null) {
            log.at(Level.FINE).log("No associated endpoint for url: %s", uri);
            return factory.newResponse404();
        }

        EndpointCaller endpoint = match.handler().getAcceptedCallerOrNull(request);
        if (endpoint == null) {
            log.at(Level.INFO).log("Endpoint does not accept the request %s for url: %s", request.method(), uri);
            return factory.newResponse404();
        }

        Caller caller = endpoint.caller();
        Object callResult;
        try {
            FullHttpRequest clientRequest = wrapRequestIfNeeded(request, endpoint.context());
            callResult = caller.call(clientRequest, match.variables());
            if (callResult == null) {
                log.at(Level.INFO).log("Request handler returned null or void: %s", caller.method());
                return convertToResponse("", endpoint.options());
            }
        } catch (ConversionError e) {
            log.at(Level.INFO).withCause(e).log("Request validation failed: %s", e.getMessage());
            return factory.newResponse400(e);
        } catch (NotFoundException e) {
            log.at(Level.WARNING).withCause(e).log("Request handler raised NOT_FOUND: %s", e.getMessage());
            return factory.newResponse404(e);
        } catch (BadRequestException e) {
            log.at(Level.WARNING).withCause(e).log("Request handler raised BAD_REQUEST: %s", e.getMessage());
            return factory.newResponse400(e);
        } catch (RedirectException e) {
            log.at(Level.WARNING).withCause(e).log("Redirecting to %s (%s): %s",
                    e.uri(), e.isPermanent() ? "permanent" : "temporary", e.getMessage());
            return factory.newResponseRedirect(e.uri(), e.isPermanent());
        } catch (Throwable e) {
            log.at(Level.SEVERE).withCause(e).log("Failed to call method: %s", caller.method());
            return factory.newResponse503("Failed to call method: %s".formatted(caller.method()), e);
        }

        return convertToResponse(callResult, endpoint.options());
    }

    @NotNull
    private static FullHttpRequest wrapRequestIfNeeded(@NotNull FullHttpRequest request, @NotNull EndpointContext context) {
        return context.rawRequest() ? request : new DefaultHttpRequestEx(request, context.constraints());
    }

    @NotNull
    @VisibleForTesting
    FullHttpResponse convertToResponse(Object callResult, EndpointOptions options) {
        if (callResult instanceof FullHttpResponse response) {
            return response;
        }

        EndpointHttp http = options.http();
        CharSequence contentType = http.hasContentType() ?
                http.contentType() :
                (options.out() == Marshal.JSON) ?
                        HttpHeaderValues.APPLICATION_JSON :
                        HttpHeaderValues.TEXT_HTML;

        if (callResult instanceof CharSequence string) {
            return factory.newResponse(string, HttpResponseStatus.OK, contentType);
        }
        if (callResult instanceof byte[] bytes) {
            return factory.newResponse(bytes, HttpResponseStatus.OK, contentType);
        }
        if (callResult instanceof char[] chars) {
            return factory.newResponse(new CharBuffer(chars), HttpResponseStatus.OK, contentType);
        }
        if (callResult instanceof InputStream stream) {
            return factory.newResponse(stream, HttpResponseStatus.OK, contentType);
        }
        if (callResult instanceof ByteBuf byteBuf) {
            return factory.newResponse(byteBuf, HttpResponseStatus.OK, contentType);
        }
        if (callResult instanceof JsonElement element) {
            return factory.newResponse(new Gson().toJson(element), HttpResponseStatus.OK, HttpHeaderValues.APPLICATION_JSON);
        }

        return switch (options.out()) {
            case JSON -> factory.newResponse(new Gson().toJson(callResult), HttpResponseStatus.OK, HttpHeaderValues.APPLICATION_JSON);
            case AS_STRING -> factory.newResponse(callResult.toString(), HttpResponseStatus.OK, contentType);
            case PROTOBUF_BINARY -> throw new UnsupportedOperationException();
            case PROTOBUF_JSON -> throw new UnsupportedOperationException();
        };
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext context, Throwable cause) {
        context.channel()
                .writeAndFlush(factory.newResponse503("Unexpected failure", cause))
                .addListener(ChannelFutureListener.CLOSE);
        log.at(Level.SEVERE).withCause(cause).log("Unexpected failure: %s", cause.getMessage());
    }
}
