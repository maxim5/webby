package io.webby.netty.dispatch.http;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.JdkFutureAdapters;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.inject.Inject;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.util.concurrent.EventExecutor;
import io.webby.netty.HttpConst;
import io.webby.netty.marshal.Marshaller;
import io.webby.netty.marshal.MarshallerFactory;
import io.webby.netty.response.EmptyHttpResponse;
import io.webby.netty.response.HttpResponseFactory;
import io.webby.netty.response.ResponseHeaders;
import io.webby.netty.response.ResponseMapper;
import io.webby.url.annotate.Marshal;
import io.webby.url.impl.EndpointOptions;
import io.webby.url.impl.EndpointView;
import io.webby.url.view.Renderer;
import io.webby.util.base.Unchecked.Consumers;
import io.webby.util.base.Unchecked.Guava;
import io.webby.util.base.Pair;
import io.webby.util.func.ThrowConsumer;
import io.webby.util.netty.EasyByteBuf;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;

import java.io.OutputStream;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Function;

import static io.webby.util.base.EasyCast.castAny;

class NettyHttpStepConverter implements ChannelContextBound {
    @Inject private HttpResponseFactory responses;
    @Inject private ResponseHeaders headers;
    @Inject private ResponseMapper mapper;
    @Inject private MarshallerFactory marshallers;

    private ChannelHandlerContext context;

    @Override
    public void bindContext(@NotNull ChannelHandlerContext context) {
        assert this.context == null : "%s is not sharable: can't be added to multiple contexts".formatted(this);
        this.context = context;
    }

    @NotNull HttpResponse convertToResponse(@NotNull Object callResult, @NotNull EndpointOptions options) throws Exception {
        HttpResponse response = buildResponseFrom(callResult, options);
        withHeaders(response, getDefaultHeaders(options), false);
        withHeaders(response, options.http().headers(), true);
        return response;
    }

    private @NotNull HttpResponse buildResponseFrom(@NotNull Object callResult,
                                                    @NotNull EndpointOptions options) throws Exception {
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
                return buildResponseFrom(future.get(), options);
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
        return responses.newResponse(marshaller.writeByteBuf(callResult), HttpResponseStatus.OK);
    }

    private <T> @NotNull HttpResponse renderResponse(@NotNull EndpointView<T> view,
                                                     @NotNull Object callResult) throws Exception {
        Renderer<T> renderer = view.renderer();
        T template = view.template();
        return switch (renderer.support()) {
            case BYTE_ARRAY -> {
                byte[] bytes = renderer.renderToBytes(template, callResult);
                yield responses.newResponse(bytes, HttpResponseStatus.OK);
            }
            case STRING -> {
                String content = renderer.renderToString(template, callResult);
                yield responses.newResponse(content, HttpResponseStatus.OK);
            }
            case BYTE_STREAM -> {
                ThrowConsumer<OutputStream, Exception> throwConsumer = renderer.renderToByteStream(template, callResult);
                Consumer<OutputStream> consumer = castAny(Consumers.rethrow(throwConsumer));
                yield addCallback(consumer);
            }
        };
    }

    private @NotNull HttpResponse addCallback(@NotNull Future<?> future, @NotNull EndpointOptions options) {
        ListenableFuture<?> listenable = (future instanceof ListenableFuture<?> listenableFuture) ?
            listenableFuture :
            JdkFutureAdapters.listenInPoolThread(future, executor());

        ListenableFuture<HttpResponse> transform = Futures.transform(
            listenable,
            Guava.rethrow(result -> convertToResponse(result != null ? result : "", options)),
            executor()
        );

        Futures.addCallback(transform, new FutureCallback<>() {
            @Override
            public void onSuccess(@Nullable HttpResponse result) {
                context.writeAndFlush(result);
            }
            @Override
            public void onFailure(@NotNull Throwable failure) {
                context.writeAndFlush(responses.newResponse500("Handler future failed: %s".formatted(future), failure));
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
                    context.write(new DefaultHttpContent(EasyByteBuf.allocate(context.alloc(), bytes)));
                }
                @Override
                public void write(byte @NotNull [] bytes, int offset, int length) {
                    context.write(new DefaultHttpContent(EasyByteBuf.allocate(context.alloc(), bytes, offset, length)));
                }
                @Override
                public void write(int b) {
                    context.write(new DefaultHttpContent(Unpooled.buffer(1).writeByte(b)));
                }
                @Override
                public void flush() {
                    context.flush();
                }
                @Override
                public void close() {
                    context.flush();  // for tests (only?)
                    context.close();  // to complete browser's waiting
                }
            })
        );

        return new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
    }

    private @NotNull EventExecutor executor() {
        return context.executor();
    }

    private @NotNull Iterable<Pair<CharSequence, CharSequence>> getDefaultHeaders(@NotNull EndpointOptions options) {
        CharSequence contentType = options.http().hasContentType() ?
            options.http().contentType() :
            (options.out() == Marshal.JSON) ?
                HttpConst.APPLICATION_JSON :
                HttpConst.TEXT_HTML;
        return headers.defaultHeaders(contentType);
    }

    @CanIgnoreReturnValue
    private static <T extends CharSequence, U> @NotNull HttpResponse withHeaders(
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
