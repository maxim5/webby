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
        FullHttpResponse response = handle(request);
        context.writeAndFlush(response);
        long millis = stopwatch.stop().elapsed(TimeUnit.MILLISECONDS);
        log.at(Level.INFO).log("%s %s: %s (%dms)", request.method(), request.uri(), response.status(), millis);
    }

    @NotNull
    private FullHttpResponse handle(@NotNull FullHttpRequest request) {
        CharBuffer uri = new CharBuffer(request.uri());
        Match<UrlBinder.Caller> match = router.routeOrNull(uri);
        if (match == null) {
            throw new RuntimeException("404: " + uri);
        }

        Object result = match.handler().call(uri, match.variables());
        if (result == null) {
            throw new RuntimeException("500: " + uri);
        }
        assert result instanceof String;

        ByteBuf content = Unpooled.copiedBuffer(result.toString(), CharsetUtil.UTF_8);
        return createResponseOK(content, HttpHeaderValues.TEXT_HTML);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext context, Throwable cause) {
        log.at(Level.WARNING).withCause(cause).log("Exception caught: %s", cause.getMessage());
        context.close();
    }

    @NotNull
    private static FullHttpResponse createResponseOK(@NotNull ByteBuf content, @NotNull AsciiString contentType) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, content);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, contentType);
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, content.readableBytes());
        return response;
    }
}
