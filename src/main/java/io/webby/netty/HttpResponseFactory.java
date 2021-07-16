package io.webby.netty;

import com.google.common.base.Throwables;
import com.google.common.flogger.FluentLogger;
import com.google.inject.Inject;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import io.webby.app.AppSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.logging.Level;

public class HttpResponseFactory {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    private static final String DEFAULT_WEB = "/web/";

    private final AppSettings settings;

    @Inject
    public HttpResponseFactory(@NotNull AppSettings settings) {
        validateSettings(settings);
        this.settings = settings;
    }

    @NotNull
    public FullHttpResponse newResponse(@NotNull CharSequence content,
                                        @NotNull HttpResponseStatus status,
                                        @NotNull CharSequence contentType) {
        return withContentType(newResponse(content, status), contentType);
    }

    @NotNull
    public FullHttpResponse newResponse(@NotNull ByteBuf content,
                                        @NotNull HttpResponseStatus status,
                                        @NotNull CharSequence contentType) {
        return withContentType(newResponse(content, status), contentType);
    }

    @NotNull
    public FullHttpResponse newResponseRedirect(@NotNull String uri, boolean permanent) {
        HttpResponseStatus status = permanent ?
                HttpResponseStatus.PERMANENT_REDIRECT :
                HttpResponseStatus.TEMPORARY_REDIRECT;
        String content = statusLine(status);
        FullHttpResponse response = newResponse(content, status);
        response.headers().add(HttpHeaderNames.LOCATION, uri);
        return response;
    }

    @NotNull
    public FullHttpResponse newResponse400(@Nullable Throwable error) {
        return newErrorResponse(HttpResponseStatus.BAD_REQUEST, null, error);
    }

    @NotNull
    public FullHttpResponse newResponse404() {
        return newResponse404(null);
    }

    @NotNull
    public FullHttpResponse newResponse404(@Nullable Throwable error) {
        return newErrorResponse(HttpResponseStatus.NOT_FOUND, null, error);
    }

    @NotNull
    public FullHttpResponse newResponse503(@NotNull String debugError, @Nullable Throwable cause) {
        return newErrorResponse(HttpResponseStatus.SERVICE_UNAVAILABLE, debugError, cause);
    }

    @NotNull
    public FullHttpResponse newErrorResponse(@NotNull HttpResponseStatus status,
                                             @Nullable String debugError,
                                             @Nullable Throwable cause) {
        String name = "%d.html".formatted(status.code());
        try {
            ByteBuf resource = getResourceAsByteBuf(name);
            if (resource == null) {
                return newResponse(statusLine(status), status, HttpHeaderValues.TEXT_HTML);
            }
            if (settings.isDevMode() && (debugError != null || cause != null)) {
                String content = resource.toString(Charset.defaultCharset()).formatted(
                        debugError != null ? debugError : "",
                        cause != null ? Throwables.getStackTraceAsString(cause) : ""
                );
                return newResponse(content, status, HttpHeaderValues.TEXT_HTML);
            }
            return newResponse(resource, status, HttpHeaderValues.TEXT_HTML);
        } catch (Exception e) {
            log.at(Level.SEVERE).withCause(e).log("Failed to read the resource: %s, returning default response", name);
            return newResponse(statusLine(status), status, HttpHeaderValues.TEXT_HTML);
        }
    }

    @NotNull
    private static FullHttpResponse newResponse(@NotNull CharSequence content, @NotNull HttpResponseStatus status) {
        ByteBuf byteBuf = Unpooled.copiedBuffer(content, CharsetUtil.UTF_8);
        return newResponse(byteBuf, status);
    }

    @NotNull
    private static FullHttpResponse newResponse(@NotNull ByteBuf byteBuf, @NotNull HttpResponseStatus status) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, byteBuf);
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, byteBuf.readableBytes());
        return response;
    }

    private static void validateSettings(@NotNull AppSettings settings) {
        if (settings.webPath() == null) {
            log.at(Level.WARNING).log("Invalid app settings: static web path is not set");
        }
        if (!new File(settings.webPath()).exists()) {
            log.at(Level.WARNING).log("Invalid app settings: static web path does not exist: %s", settings.webPath());
        }
    }

    private static String statusLine(@NotNull HttpResponseStatus status) {
        return "%d %s".formatted(status.code(), status.codeAsText());
    }

    @Nullable
    private ByteBuf getResourceAsByteBuf(@NotNull String name) throws IOException {
        File file = new File(settings.webPath(), name);
        if (file.exists()) {
            return fileToByteBuf(file);
        }

        if (settings.isDevMode()) {
            String resource = "%s/%s".formatted(DEFAULT_WEB, name);
            InputStream inputStream = getClass().getResourceAsStream(resource);
            if (inputStream == null) {
                throw new FileNotFoundException("The resource %s is not found in classpath".formatted(resource));
            }
            return Unpooled.wrappedBuffer(inputStream.readAllBytes());
        }

        return null;
    }

    @NotNull
    private static ByteBuf fileToByteBuf(@NotNull File file) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(file);
        FileChannel fileChannel = fileInputStream.getChannel();
        MappedByteBuffer mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, file.length());
        return Unpooled.wrappedBuffer(mappedByteBuffer);
    }

    @NotNull
    private static FullHttpResponse withContentType(@NotNull FullHttpResponse response, @NotNull CharSequence contentType) {
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, contentType);
        return response;
    }
}
