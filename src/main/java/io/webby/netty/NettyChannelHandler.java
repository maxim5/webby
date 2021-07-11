package io.webby.netty;

import com.google.common.base.Stopwatch;
import com.google.common.flogger.FluentLogger;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.AsciiString;
import io.netty.util.CharsetUtil;
import io.routekit.Match;
import io.routekit.Router;
import io.routekit.util.CharBuffer;
import io.webby.url.UrlRouter;
import io.webby.url.caller.Caller;
import io.webby.url.caller.ValidationError;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class NettyChannelHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    private final Router<Caller> router;

    public NettyChannelHandler(@NotNull UrlRouter urlRouter) {
        this.router = urlRouter.getRouter();
    }

    @Override
    protected void channelRead0(@NotNull ChannelHandlerContext context, @NotNull FullHttpRequest request) {
        Stopwatch stopwatch = Stopwatch.createStarted();

        FullHttpResponse response;
        try {
            response = handle(request);
        } catch (Throwable throwable) {
            response = newResponse503("Unexpected failure", throwable);
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
        Match<Caller> match = router.routeOrNull(uri);
        if (match == null) {
            return newResponse404();
        }

        Object result;
        try {
            result = match.handler().call(request, match.variables());
            if (result == null) {
                return newResponse503("Request handler returned null: %s".formatted(match.handler().method()));
            }
        } catch (ValidationError e) {
            log.at(Level.INFO).withCause(e).log("Request validation failed: %s", e.getMessage());
            return newResponse400();
        } catch (NotFoundException e) {
            log.at(Level.WARNING).withCause(e).log("Request handler returned: %s", e.getMessage());
            return newResponse404();
        } catch (Exception e) {
            log.at(Level.SEVERE).withCause(e).log("Failed to call method: %s", match.handler().method());
            return newResponse503("Failed to call method: %s".formatted(match.handler().method()), e);
        }

        AsciiString contentType = HttpHeaderValues.TEXT_HTML;  // TODO: json
        if (result instanceof CharSequence sequence) {
            return newResponse(sequence, HttpResponseStatus.OK, contentType);
        }
        if (result instanceof FullHttpResponse response) {
            return response;
        }
        return newResponse(result.toString(), HttpResponseStatus.OK, contentType);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext context, Throwable cause) {
        context.channel().writeAndFlush(newResponse503("Unexpected failure", cause)).addListener(ChannelFutureListener.CLOSE);
        log.at(Level.SEVERE).withCause(cause).log("Unexpected failure: %s", cause.getMessage());
    }

    @NotNull
    private static FullHttpResponse newResponse400() {
        return newResponse("<h1>404: Bad Request</h1>", HttpResponseStatus.BAD_REQUEST, HttpHeaderValues.TEXT_HTML);
    }

    @NotNull
    private static FullHttpResponse newResponse404() {
        return newResponse("<h1>404: Not Found</h1>", HttpResponseStatus.NOT_FOUND, HttpHeaderValues.TEXT_HTML);
    }

    @NotNull
    private static FullHttpResponse newResponse503(@NotNull String debugError) {
        return newResponse503(debugError, null);
    }

    @NotNull
    private static FullHttpResponse newResponse503(@NotNull String debugError, @Nullable Throwable throwable) {
        // TODO: hide debug info in prod
        return newResponse(
                "<h1>503: Service Unavailable</h1><p>%s</p><p>%s</p>".formatted(debugError, throwable),
                HttpResponseStatus.SERVICE_UNAVAILABLE,
                HttpHeaderValues.TEXT_HTML);
    }

    @NotNull
    private static FullHttpResponse newResponse(@NotNull CharSequence content,
                                                HttpResponseStatus status,
                                                AsciiString contentType) {
        ByteBuf byteBuf = Unpooled.copiedBuffer(content, CharsetUtil.UTF_8);
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, byteBuf);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, contentType);
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, byteBuf.readableBytes());
        return response;
    }
}
