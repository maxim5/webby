package io.webby.netty;

import com.google.common.base.Stopwatch;
import com.google.common.flogger.FluentLogger;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.inject.Inject;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.routekit.Match;
import io.routekit.Router;
import io.routekit.util.CharBuffer;
import io.webby.netty.exceptions.BadRequestException;
import io.webby.netty.exceptions.NotFoundException;
import io.webby.netty.exceptions.RedirectException;
import io.webby.netty.request.DefaultHttpRequestEx;
import io.webby.netty.response.HttpResponseFactory;
import io.webby.netty.response.ResponseMapper;
import io.webby.url.annotate.Marshal;
import io.webby.url.caller.Caller;
import io.webby.url.convert.ConversionError;
import io.webby.url.impl.*;
import io.webby.url.view.Renderer;
import io.webby.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.logging.Level;

public class NettyChannelHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    private final HttpResponseFactory factory;
    private final ResponseMapper mapper;
    private final Router<RouteEndpoint> router;

    @Inject
    public NettyChannelHandler(@NotNull HttpResponseFactory factory,
                               @NotNull ResponseMapper mapper,
                               @NotNull UrlRouter urlRouter) {
        this.factory = factory;
        this.mapper = mapper;
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
        CharBuffer path = extractPath(request);
        Match<RouteEndpoint> match = router.routeOrNull(path);
        if (match == null) {
            log.at(Level.FINE).log("No associated endpoint for url: %s", path);
            return factory.newResponse404();
        }

        EndpointCaller endpoint = match.handler().getAcceptedCallerOrNull(request);
        if (endpoint == null) {
            log.at(Level.INFO).log("Endpoint does not accept the request %s for url: %s", request.method(), path);
            return factory.newResponse404();
        }

        Caller caller = endpoint.caller();
        try {
            FullHttpRequest clientRequest = wrapRequestIfNeeded(request, endpoint.context());
            Object callResult = caller.call(clientRequest, match.variables());
            if (callResult == null) {
                if (endpoint.context().isVoid()) {
                    log.at(Level.FINE).log("Request handler is void, transforming into empty string");
                } else {
                    log.at(Level.INFO).log("Request handler returned null: %s", caller.method());
                }
                return createResponse("", endpoint.options());
            }
            return createResponse(callResult, endpoint.options());
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
    }

    @VisibleForTesting
    @NotNull
    CharBuffer extractPath(@NotNull FullHttpRequest request) {
        CharBuffer uri = new CharBuffer(request.uri());
        // TODO: handle '#'
        // TODO: handle trailing slash (settings)
        return uri.substringUntil(uri.indexOf('?', 0, uri.length()));
    }

    @VisibleForTesting
    @NotNull
    static FullHttpRequest wrapRequestIfNeeded(@NotNull FullHttpRequest request, @NotNull EndpointContext context) {
        return context.isRawRequest() ? request : new DefaultHttpRequestEx(request, context.constraints());
    }

    @VisibleForTesting
    @NotNull
    FullHttpResponse createResponse(@NotNull Object callResult, @NotNull EndpointOptions options) throws Exception {
        FullHttpResponse response = convertToResponse(callResult, options);
        withHeaders(response, getDefaultHeaders(options), false);
        withHeaders(response, options.http().headers(), true);
        return response;
    }

    @VisibleForTesting
    @NotNull
    FullHttpResponse convertToResponse(@NotNull Object callResult, @NotNull EndpointOptions options) throws Exception {
        if (callResult instanceof FullHttpResponse response) {
            return response;
        }

        EndpointView<?> view = options.view();
        if (view != null) {
            return renderResponse(view, callResult);
        }

        Function<Object, FullHttpResponse> responseFunction = mapper.mapInstance(callResult);
        if (responseFunction != null) {
            return responseFunction.apply(callResult);
        }
        if (callResult instanceof JsonElement element) {
            return factory.newResponse(new Gson().toJson(element), HttpResponseStatus.OK, HttpHeaderValues.APPLICATION_JSON);
        }

        return switch (options.out()) {
            case JSON -> {
                String json = new Gson().toJson(callResult);
                yield factory.newResponse(json, HttpResponseStatus.OK, HttpHeaderValues.APPLICATION_JSON);
            }
            case AS_STRING -> factory.newResponse(callResult.toString(), HttpResponseStatus.OK);
            case PROTOBUF_BINARY -> throw new UnsupportedOperationException();
            case PROTOBUF_JSON -> throw new UnsupportedOperationException();
        };
    }

    @VisibleForTesting
    @NotNull
    <T> FullHttpResponse renderResponse(@NotNull EndpointView<T> view, @NotNull Object callResult) throws Exception {
        Renderer<T> renderer = view.renderer();
        T template = view.template();
        return switch (renderer.support()) {
            case BYTE_ARRAY -> {
                byte[] bytes = renderer.renderToBytes(template, callResult);
                yield factory.newResponse(bytes, HttpResponseStatus.OK);
            }
            case STRING -> {
                String content = renderer.renderToString(template, callResult);
                yield factory.newResponse(content, HttpResponseStatus.OK);
            }
            case BYTE_STREAM -> throw new UnsupportedOperationException();
        };
    }

    @VisibleForTesting
    @NotNull
    Iterable<Pair<CharSequence, CharSequence>> getDefaultHeaders(@NotNull EndpointOptions options) {
        CharSequence contentType = options.http().hasContentType() ?
                options.http().contentType() :
                (options.out() == Marshal.JSON) ?
                        HttpHeaderValues.APPLICATION_JSON :
                        HttpHeaderValues.TEXT_HTML;

        return List.of(
            Pair.of(HttpHeaderNames.CONTENT_TYPE, contentType),
            Pair.of(HttpHeaderNames.X_FRAME_OPTIONS, "SAMEORIGIN"),  // iframe attack
            Pair.of("X-XSS-Protection", "1;mode=block"),  // XSS Filtering
            Pair.of(HttpHeaderNames.CONTENT_SECURITY_POLICY, "script-src 'self'"),  // Whitelisting Sources
            Pair.of("X-Content-Type-Options", "nosniff")  // MIME-sniffing attacks

            // Content-Security-Policy: upgrade-insecure-requests
            // Strict-Transport-Security: max-age=1000; includeSubDomains; preload
        );
    }

    @VisibleForTesting
    @NotNull
    @CanIgnoreReturnValue
    static <T extends CharSequence, U> FullHttpResponse withHeaders(
            @NotNull FullHttpResponse response,
            @NotNull Iterable<Pair<T, U>> values,
            boolean canOverwrite) {
        return HttpResponseFactory.withHeaders(response, headers -> {
            for (Pair<T, U> header : values) {
                if (canOverwrite || !headers.contains(header.getKey()))
                    headers.set(header.getKey(), header.getValue());
            }
        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext context, Throwable cause) {
        context.channel()
                .writeAndFlush(factory.newResponse503("Unexpected failure", cause))
                .addListener(ChannelFutureListener.CLOSE);
        log.at(Level.SEVERE).withCause(cause).log("Unexpected failure: %s", cause.getMessage());
    }
}
