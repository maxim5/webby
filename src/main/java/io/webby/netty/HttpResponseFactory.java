package io.webby.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HttpResponseFactory {
    @NotNull
    public static FullHttpResponse newResponse400() {
        return newResponse("<h1>400: Bad Request</h1>", HttpResponseStatus.BAD_REQUEST, HttpHeaderValues.TEXT_HTML);
    }

    @NotNull
    public static FullHttpResponse newResponse404() {
        return newResponse("<h1>404: Not Found</h1>", HttpResponseStatus.NOT_FOUND, HttpHeaderValues.TEXT_HTML);
    }

    @NotNull
    public static FullHttpResponse newResponseRedirect(@NotNull String uri, boolean permanent) {
        HttpResponseStatus status = permanent ?
                HttpResponseStatus.PERMANENT_REDIRECT :
                HttpResponseStatus.TEMPORARY_REDIRECT;
        String content = "%d %s".formatted(status.code(), status.codeAsText());
        FullHttpResponse response = newResponse(content, status, HttpHeaderValues.NONE);
        response.headers().add(HttpHeaderNames.LOCATION, uri);
        return response;
    }

    @NotNull
    public static FullHttpResponse newResponse503(@NotNull String debugError) {
        return newResponse503(debugError, null);
    }

    @NotNull
    public static FullHttpResponse newResponse503(@NotNull String debugError, @Nullable Throwable throwable) {
        // TODO: hide debug info in prod
        return newResponse(
                "<h1>503: Service Unavailable</h1><p>%s</p><p>%s</p>".formatted(debugError, throwable),
                HttpResponseStatus.SERVICE_UNAVAILABLE,
                HttpHeaderValues.TEXT_HTML);
    }

    @NotNull
    public static FullHttpResponse newResponse(@NotNull CharSequence content,
                                               @NotNull HttpResponseStatus status,
                                               @NotNull CharSequence contentType) {
        ByteBuf byteBuf = Unpooled.copiedBuffer(content, CharsetUtil.UTF_8);
        return newResponse(byteBuf, status, contentType);
    }

    @NotNull
    public static FullHttpResponse newResponse(@NotNull ByteBuf byteBuf,
                                               @NotNull HttpResponseStatus status,
                                               @NotNull CharSequence contentType) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, byteBuf);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, contentType);
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, byteBuf.readableBytes());
        return response;
    }
}
