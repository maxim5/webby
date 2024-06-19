package io.spbx.webby.netty.dispatch.http;

import com.google.common.base.Stopwatch;
import com.google.common.flogger.FluentLogger;
import com.google.inject.Inject;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.util.ReferenceCountUtil;
import io.spbx.webby.db.sql.ThreadLocalConnector;
import io.spbx.webby.netty.HttpConst;
import io.spbx.webby.netty.response.AsyncResponse;
import io.spbx.webby.netty.response.EmptyHttpResponse;
import io.spbx.webby.netty.response.HttpResponseFactory;
import io.spbx.webby.netty.response.StreamingHttpResponse;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class NettyHttpHandler extends ChannelInboundHandlerAdapter {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    @Inject private HttpResponseFactory responses;
    @Inject private NettyHttpStack stack;

    private ChannelHandlerContext context;

    @Override
    public void handlerAdded(@NotNull ChannelHandlerContext context) {
        // See https://stackoverflow.com/questions/46508433/concurrency-in-netty
        assert this.context == null : "%s is not sharable: can't be added to multiple contexts".formatted(this);
        this.context = context;
        stack.bindContext(this.context);
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
            response = stack.processIncoming(request);
        } catch (Throwable throwable) {
            response = responses.newResponse500("Unexpected failure", throwable);
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
            context.writeAndFlush(response);
        }

        return response;
    }

    @Override
    public void exceptionCaught(@NotNull ChannelHandlerContext context, @NotNull Throwable cause) {
        context.channel()
            .writeAndFlush(responses.newResponse500("Unexpected failure", cause))
            .addListener(ChannelFutureListener.CLOSE);
        log.at(Level.SEVERE).withCause(cause).log("Unexpected failure: %s", cause.getMessage());
    }

    private static void cleanupWorkingThread() {
        ThreadLocalConnector.cleanupIfNecessary();
    }
}
