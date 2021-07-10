package io.webby.netty;

import com.google.common.base.Stopwatch;
import com.google.common.flogger.FluentLogger;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.AsciiString;
import io.netty.util.CharsetUtil;
import io.routekit.Match;
import io.routekit.Router;
import io.routekit.util.CharBuffer;
import io.webby.url.UrlBinder;
import io.webby.url.UrlRouter;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class NettyChannelHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    private final Router<UrlBinder.Caller> router;

    public NettyChannelHandler(@NotNull UrlRouter urlRouter) {
        this.router = urlRouter.getRouter();
    }

    @Override
    protected void channelRead0(@NotNull ChannelHandlerContext context, @NotNull FullHttpRequest request) {
        Stopwatch stopwatch = Stopwatch.createStarted();

        FullHttpResponse response;
        try {
            response = handle(request);
        } catch (NotFoundException e) {
            log.at(Level.WARNING).withCause(e).log("Request handler returned: %s", e.getMessage());
            response = newResponse404();
        } catch (Throwable throwable) {
            log.at(Level.WARNING).withCause(throwable).log("Request handler failed: %s", throwable.getMessage());
            response = newResponse503(throwable.getMessage());
        }
        context.writeAndFlush(response);

        long millis = stopwatch.stop().elapsed(TimeUnit.MILLISECONDS);
        log.at(Level.INFO).log("%s %s: %s (%d ms)", request.method(), request.uri(), response.status(), millis);
    }

    @NotNull
    private FullHttpResponse handle(@NotNull FullHttpRequest request) {
        CharBuffer uri = new CharBuffer(request.uri());
        Match<UrlBinder.Caller> match = router.routeOrNull(uri);
        if (match == null) {
            return newResponse404();
        }

        Object result = match.handler().call(uri, match.variables());
        if (result == null) {
            return newResponse503("Failed to process the request");
        }
        assert result instanceof String;

        return newResponse(result.toString(), HttpResponseStatus.OK, HttpHeaderValues.TEXT_HTML);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext context, Throwable cause) {
        log.at(Level.SEVERE).withCause(cause).log("Exception caught: %s", cause.getMessage());
        context.close();
    }

    @NotNull
    private static FullHttpResponse newResponse404() {
        return newResponse("<h1>404: Not Found</h1>", HttpResponseStatus.NOT_FOUND, HttpHeaderValues.TEXT_HTML);
    }

    // TODO: hide debug info in prod
    @NotNull
    private static FullHttpResponse newResponse503(@NotNull String debugError) {
        return newResponse(
                "<h1>503: Service Unavailable</h1><p>%s</p>".formatted(debugError),
                HttpResponseStatus.SERVICE_UNAVAILABLE,
                HttpHeaderValues.TEXT_HTML);
    }

    @NotNull
    private static FullHttpResponse newResponse(@NotNull String content, HttpResponseStatus status, AsciiString contentType) {
        ByteBuf byteBuf = Unpooled.copiedBuffer(content, CharsetUtil.UTF_8);
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, byteBuf);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, contentType);
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, byteBuf.readableBytes());
        return response;
    }
}
