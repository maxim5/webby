package io.webby.netty;

import com.google.common.base.Stopwatch;
import com.google.common.flogger.FluentLogger;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.JdkFutureAdapters;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.inject.Inject;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.*;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.EventExecutor;
import io.routekit.Match;
import io.routekit.Router;
import io.routekit.util.CharArray;
import io.routekit.util.MutableCharArray;
import io.webby.app.Settings;
import io.webby.db.sql.ThreadLocalConnector;
import io.webby.netty.errors.ServeException;
import io.webby.netty.intercept.Interceptors;
import io.webby.netty.marshal.Marshaller;
import io.webby.netty.marshal.MarshallerFactory;
import io.webby.netty.request.DefaultHttpRequestEx;
import io.webby.netty.response.*;
import io.webby.url.annotate.Marshal;
import io.webby.url.caller.Caller;
import io.webby.url.convert.ConversionError;
import io.webby.url.impl.Endpoint;
import io.webby.url.impl.EndpointOptions;
import io.webby.url.impl.EndpointView;
import io.webby.url.impl.RouteEndpoint;
import io.webby.url.view.Renderer;
import io.webby.util.base.Unchecked.Consumers;
import io.webby.util.base.Unchecked.Guava;
import io.webby.util.collect.Pair;
import io.webby.util.func.ThrowConsumer;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;

import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;

import static io.webby.util.base.EasyCast.castAny;

public class NettyHttpHandler extends ChannelInboundHandlerAdapter {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    @Inject private Settings settings;
    @Inject private Interceptors interceptors;
    @Inject private HttpResponseFactory factory;
    @Inject private ResponseHeaders headers;
    @Inject private ResponseMapper mapper;
    @Inject private Router<RouteEndpoint> router;
    @Inject private MarshallerFactory marshallers;

    private ChannelHandlerContext context;
    private Channel channel;

    @Override
    public void handlerAdded(@NotNull ChannelHandlerContext ctx) {
        // See https://stackoverflow.com/questions/46508433/concurrency-in-netty
        assert context == null && channel == null : "%s is not sharable: can't be added to multiple contexts".formatted(this);
        context = ctx;
        channel = ctx.channel();
    }

    @Override
    public void channelActive(@NotNull ChannelHandlerContext context) {
        log.at(Level.FINER).log("Request Channel is active: %s", context);
    }

    @Override
    public void channelInactive(@NotNull ChannelHandlerContext context) {
        log.at(Level.FINER).log("Request Channel is inactive: %s", context);
    }

    @Override
    public void channelRead(@NotNull ChannelHandlerContext context, @NotNull Object message) {
        assert this.context == context : "Context mismatch: %s != %s".formatted(this.context, context);

        if (message instanceof FullHttpRequest request) {
            try {
                Stopwatch stopwatch = Stopwatch.createStarted();
                HttpResponse response = processIncoming(request);
                long millis = stopwatch.stop().elapsed(TimeUnit.MILLISECONDS);
                log.at(Level.INFO).log("%s %s: %s (%s b | %d ms)",
                                       request.method(), request.uri(), response.status(),
                                       response.headers().get(HttpConst.CONTENT_LENGTH), millis);
            } finally {
                ReferenceCountUtil.release(request);
                cleanupWorkingThread();
            }
        } else {
            context.fireChannelRead(message);
        }
    }

    protected @NotNull HttpResponse processIncoming(@NotNull FullHttpRequest request) {
        HttpResponse response;
        try {
            response = handle(request);
        } catch (Throwable throwable) {
            cleanupAfterFailure();
            response = factory.newResponse500("Unexpected failure", throwable);
            log.at(Level.SEVERE).withCause(throwable).log("Unexpected failure: %s", throwable.getMessage());
        }

        if (response instanceof AsyncResponse) {
            if (response instanceof StreamingHttpResponse streaming) {
                // TODO: streaming must be closed
                // The following code doesn't work for http://localhost:8888/headers/zip
                // channel.write(streaming).addListener(future -> channel.writeAndFlush(streaming.chunkedContent()));
                // Something's wrong with the executor.
                context.write(streaming);
                context.writeAndFlush(streaming.chunkedContent());
            } else if (response instanceof EmptyHttpResponse) {
                log.at(Level.FINE).log("Response to be handled async");
            }
        } else {
            channel.writeAndFlush(response);
        }

        return response;
    }

    @Override
    public void exceptionCaught(@NotNull ChannelHandlerContext context, @NotNull Throwable cause) {
        cleanupAfterFailure();
        context.channel()
                .writeAndFlush(factory.newResponse500("Unexpected failure", cause))
                .addListener(ChannelFutureListener.CLOSE);
        log.at(Level.SEVERE).withCause(cause).log("Unexpected failure: %s", cause.getMessage());
    }

    @VisibleForTesting
    @NotNull HttpResponse handle(@NotNull FullHttpRequest request) {
        DecoderResult decoderResult = request.decoderResult();
        if (decoderResult.isFailure()) {
            log.at(Level.INFO).log("Failed to decode request: %s", decoderResult);
            return factory.newResponse400(decoderResult.cause());
        }

        CharArray path = extractPath(request.uri());
        Match<RouteEndpoint> match = router.routeOrNull(path);
        if (match == null) {
            log.at(Level.FINE).log("No associated endpoint for url: %s", path);
            return factory.newResponse404();
        }

        Endpoint endpoint = match.handler().getAcceptedEndpointOrNull(request);
        if (endpoint == null) {
            log.at(Level.INFO).log("Endpoint does not accept the request %s for url: %s", request.method(), path);
            return factory.newResponse404();
        }

        if (endpoint.context().bypassInterceptors()) {
            return call(request, match, endpoint);
        } else {
            DefaultHttpRequestEx requestEx = interceptors.createRequest(request, channel, endpoint.context());
            HttpResponse intercepted = interceptors.enter(requestEx, endpoint);
            if (intercepted != null) {
                interceptors.cleanup();  // consider try-finally?
                return intercepted;
            }
            HttpResponse response = call(requestEx, match, endpoint);
            return interceptors.exit(requestEx, response);
        }
    }

    private void cleanupAfterFailure() {
        interceptors.cleanup();
    }

    private static void cleanupWorkingThread() {
        ThreadLocalConnector.cleanupIfNecessary();
    }

    private @NotNull HttpResponse call(@NotNull FullHttpRequest request,
                                       @NotNull Match<RouteEndpoint> match,
                                       @NotNull Endpoint endpoint) {
        Caller caller = endpoint.caller();
        try {
            Object callResult = caller.call(request, match.variables());
            if (callResult == null) {
                if (endpoint.context().isVoid()) {
                    log.at(Level.FINE).log("Request handler is void, transforming into empty string");
                } else {
                    log.at(Level.WARNING).log("Request handler returned null: %s", caller.method());
                }
                return createResponse("", endpoint.options());
            }
            return createResponse(callResult, endpoint.options());
        } catch (Throwable e) {
            return callException(e, caller);
        }
    }

    private @NotNull HttpResponse callException(@NotNull Throwable error, @NotNull Caller caller) {
        if (error instanceof ConversionError) {
            log.at(Level.INFO).withCause(error).log("Request validation failed: %s", error.getMessage());
            return factory.newResponse400(error);
        }
        if (error instanceof ServeException e) {
            return factory.handleServeException(e, "Request handler %s".formatted(caller.method()));
        }
        if (error instanceof InvocationTargetException) {
            return callException(error.getCause(), caller);
        }

        log.at(Level.SEVERE).withCause(error).log("Failed to call method: %s", caller.method());
        return factory.newResponse500("Failed to call method: %s".formatted(caller.method()), error);
    }

    @VisibleForTesting
    @NotNull HttpResponse createResponse(@NotNull Object callResult, @NotNull EndpointOptions options) throws Exception {
        HttpResponse response = convertToResponse(callResult, options);
        withHeaders(response, getDefaultHeaders(options), false);
        withHeaders(response, options.http().headers(), true);
        return response;
    }

    @VisibleForTesting
    @NotNull HttpResponse convertToResponse(@NotNull Object callResult, @NotNull EndpointOptions options) throws Exception {
        if (callResult instanceof HttpResponse response) {
            return response;
        }

        EndpointView<?> view = options.view();
        if (view != null) {
            return renderResponse(view, callResult);
        }

        Function<Object, HttpResponse> responseFunction = mapper.mapInstance(callResult);
        if (responseFunction != null) {
            return responseFunction.apply(callResult);
        }
        if (callResult instanceof Future<?> future) {
            if (future.isDone()) {
                return convertToResponse(future.get(), options);
            }
            return addCallback(future, options);
        }
        if (callResult instanceof Consumer<?>) {
            Consumer<OutputStream> consumer = castAny(callResult);
            return addCallback(consumer);
        }
        if (callResult instanceof ThrowConsumer<?, ?> throwConsumer) {
            Consumer<OutputStream> consumer = castAny(Consumers.rethrow(throwConsumer));
            return addCallback(consumer);
        }

        Marshaller marshaller = marshallers.getMarshaller(options.out());
        return factory.newResponse(marshaller.writeByteBuf(callResult), HttpResponseStatus.OK);
    }

    @VisibleForTesting
    <T> @NotNull HttpResponse renderResponse(@NotNull EndpointView<T> view, @NotNull Object callResult) throws Exception {
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
            case BYTE_STREAM -> {
                ThrowConsumer<OutputStream, Exception> throwConsumer = renderer.renderToByteStream(template, callResult);
                Consumer<OutputStream> consumer = castAny(Consumers.rethrow(throwConsumer));
                yield addCallback(consumer);
            }
        };
    }

    @SuppressWarnings("UnstableApiUsage")
    private @NotNull HttpResponse addCallback(@NotNull Future<?> future, @NotNull EndpointOptions options) {
        ListenableFuture<?> listenable = (future instanceof ListenableFuture<?> listenableFuture) ?
                listenableFuture :
                JdkFutureAdapters.listenInPoolThread(future, executor());

        ListenableFuture<HttpResponse> transform = Futures.transform(
            listenable,
            Guava.rethrow(result -> createResponse(result != null ? result : "", options)),
            executor()
        );

        Futures.addCallback(transform, new FutureCallback<>() {
            @Override
            public void onSuccess(@Nullable HttpResponse result) {
                channel.writeAndFlush(result);
            }
            @Override
            public void onFailure(@NotNull Throwable failure) {
                channel.writeAndFlush(factory.newResponse500("Handler future failed: %s".formatted(future), failure));
            }
        }, executor());

        return EmptyHttpResponse.INSTANCE;
    }

    private @NotNull HttpResponse addCallback(@NotNull Consumer<OutputStream> consumer) {
        // TODO: use ChunkOutputStream
        Future<?> future = executor().submit(() ->
            consumer.accept(new OutputStream() {
                @Override
                public void write(byte @NotNull [] bytes) {
                    channel.write(new DefaultHttpContent(Unpooled.wrappedBuffer(bytes)));
                }
                @Override
                public void write(byte @NotNull [] bytes, int offset, int length) {
                    channel.write(new DefaultHttpContent(Unpooled.wrappedBuffer(bytes, offset, length)));
                }
                @Override
                public void write(int b) {
                    channel.write(new DefaultHttpContent(Unpooled.buffer(1).writeByte(b)));
                }
                @Override
                public void flush() {
                    channel.flush();
                }
                @Override
                public void close() {
                    channel.flush();  // for tests (only?)
                    channel.close();  // to complete browser's waiting
                }
            })
        );

        return new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
    }

    private @NotNull EventExecutor executor() {
        return context.executor();
    }

    @VisibleForTesting
    @NotNull CharArray extractPath(@NotNull String uri) {
        MutableCharArray path = new MutableCharArray(uri);
        path.offsetEnd(path.length() - path.indexOfAny('?', '#', 0, path.length()));
        if (settings.getBoolProperty("netty.url.trailing.slash.ignore") && path.length() > 1) {
            path.offsetSuffix('/');
        }
        return path;
    }

    @VisibleForTesting
    @NotNull Iterable<Pair<CharSequence, CharSequence>> getDefaultHeaders(@NotNull EndpointOptions options) {
        CharSequence contentType = options.http().hasContentType() ?
                options.http().contentType() :
                (options.out() == Marshal.JSON) ?
                        HttpConst.APPLICATION_JSON :
                        HttpConst.TEXT_HTML;
        return headers.defaultHeaders(contentType);
    }

    @VisibleForTesting
    @CanIgnoreReturnValue
    static <T extends CharSequence, U> @NotNull HttpResponse withHeaders(
            @NotNull HttpResponse response,
            @NotNull Iterable<Pair<T, U>> values,
            boolean canOverwrite) {
        return HttpResponseFactory.withHeaders(response, headers -> {
            for (Pair<T, U> header : values) {
                if (canOverwrite || !headers.contains(header.getKey()))
                    headers.set(header.getKey(), header.getValue());
            }
        });
    }
}
