package io.webby.netty.response;

import com.google.common.base.Throwables;
import com.google.common.flogger.FluentLogger;
import com.google.inject.Inject;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import io.netty.handler.stream.ChunkedFile;
import io.netty.handler.stream.ChunkedStream;
import io.webby.app.Settings;
import io.webby.netty.HttpConst;
import io.webby.netty.errors.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;
import java.util.logging.Level;

public class HttpResponseFactory {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    private static final String DEFAULT_WEB = "/web";

    @Inject private Settings settings;
    @Inject private StaticServing staticServing;
    @Inject private ResponseHeaders headers;

    public @NotNull FullHttpResponse newResponse(@NotNull CharSequence content, @NotNull HttpResponseStatus status) {
        ByteBuf byteBuf = Unpooled.copiedBuffer(content, settings.charset());
        return newResponse(byteBuf, status);
    }

    public @NotNull FullHttpResponse newResponse(@NotNull CharSequence content,
                                                 @NotNull HttpResponseStatus status,
                                                 @NotNull CharSequence contentType) {
        return withContentType(newResponse(content, status), headers.ensureCharset(contentType));
    }

    public @NotNull FullHttpResponse newResponse(@NotNull ByteBuf byteBuf, @NotNull HttpResponseStatus status) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, byteBuf);
        response.headers().set(HttpConst.CONTENT_LENGTH, byteBuf.readableBytes());
        return response;
    }

    public @NotNull FullHttpResponse newResponse(@NotNull ByteBuf content,
                                                 @NotNull HttpResponseStatus status,
                                                 @NotNull CharSequence contentType) {
        return withContentType(newResponse(content, status), headers.ensureCharset(contentType));
    }

    public @NotNull FullHttpResponse newResponse(byte[] content, @NotNull HttpResponseStatus status) {
        ByteBuf byteBuf = Unpooled.wrappedBuffer(content);
        return newResponse(byteBuf, status);
    }

    public @NotNull StreamingHttpResponse newResponse(@NotNull InputStream content, @NotNull HttpResponseStatus status) {
        ChunkedStream stream = new ChunkedStream(content);
        StreamingHttpResponse response = new StreamingHttpResponse(HttpVersion.HTTP_1_1, status, stream);
        response.headers().set(HttpHeaderNames.TRANSFER_ENCODING, HttpHeaderValues.CHUNKED);
        return response;
    }

    public @NotNull StreamingHttpResponse newResponse(@NotNull File content, @NotNull HttpResponseStatus status)
            throws IOException {
        ChunkedFile chunkedFile = new ChunkedFile(content);
        StreamingHttpResponse response = new StreamingHttpResponse(HttpVersion.HTTP_1_1, status, chunkedFile);
        response.headers().set(HttpHeaderNames.TRANSFER_ENCODING, HttpHeaderValues.CHUNKED);
        return response;
    }

    public @NotNull FullHttpResponse newResponse304() {
        return newResponse(statusLine(HttpResponseStatus.NOT_MODIFIED), HttpResponseStatus.NOT_MODIFIED);
    }

    public @NotNull FullHttpResponse newResponseRedirect(@NotNull String uri, boolean permanent) {
        HttpResponseStatus status = permanent ?
                HttpResponseStatus.PERMANENT_REDIRECT :
                HttpResponseStatus.TEMPORARY_REDIRECT;
        String content = statusLine(status);
        FullHttpResponse response = newResponse(content, status);
        response.headers().add(HttpConst.LOCATION, uri);
        return response;
    }

    public @NotNull FullHttpResponse newResponse400(@Nullable Throwable error) {
        return newErrorResponse(HttpResponseStatus.BAD_REQUEST, null, error);
    }

    public @NotNull FullHttpResponse newResponse401(@Nullable Throwable error) {
        return newErrorResponse(HttpResponseStatus.UNAUTHORIZED, null, error);
    }

    public @NotNull FullHttpResponse newResponse403(@Nullable Throwable error) {
        return newErrorResponse(HttpResponseStatus.FORBIDDEN, null, error);
    }

    public @NotNull FullHttpResponse newResponse404() {
        return newResponse404(null);
    }

    public @NotNull FullHttpResponse newResponse404(@Nullable Throwable error) {
        return newErrorResponse(HttpResponseStatus.NOT_FOUND, null, error);
    }

    public @NotNull FullHttpResponse newResponse500(@NotNull String debugError, @Nullable Throwable cause) {
        return newErrorResponse(HttpResponseStatus.INTERNAL_SERVER_ERROR, debugError, cause);
    }

    public @NotNull FullHttpResponse newResponse503(@NotNull String debugError, @Nullable Throwable cause) {
        return newErrorResponse(HttpResponseStatus.SERVICE_UNAVAILABLE, debugError, cause);
    }

    public @NotNull FullHttpResponse newErrorResponse(@NotNull HttpResponseStatus status,
                                                      @Nullable String debugError,
                                                      @Nullable Throwable cause) {
        // headers().set(CONNECTION, HttpHeaderValues.CLOSE);

        String name = "%d.html".formatted(status.code());
        try {
            ByteBuf clientError = staticServing.getByteBufOrNull(name);
            if (settings.isDevMode()) {
                ByteBuf resource = (clientError != null) ? clientError : getDefaultResource(name);
                String content = resource.toString(settings.charset()).formatted(
                        debugError != null ? debugError : "",
                        cause != null ? Throwables.getStackTraceAsString(cause) : ""
                );
                return newResponse(content, status, HttpConst.TEXT_HTML);
            } else {
                if (clientError != null) {
                    return newResponse(clientError, status, HttpConst.TEXT_HTML);
                }
            }
        } catch (Exception e) {
            log.at(Level.SEVERE).withCause(e).log("Failed to read the resource: %s, returning default response", name);
        }
        return newResponse(statusLine(status), status, HttpConst.TEXT_HTML);
    }

    public @NotNull FullHttpResponse handleServeException(@NotNull ServeException exception, @NotNull String source) {
        try {
            throw exception;
        } catch (BadRequestException e) {
            log.at(Level.INFO).withCause(e).log("%s raised BAD_REQUEST: %s", source, e.getMessage());
            return newResponse400(e);
        } catch (UnauthorizedException e) {
            log.at(Level.INFO).withCause(e).log("%s raised UNAUTHORIZED: %s", source, e.getMessage());
            return newResponse401(e);
        } catch (ForbiddenException e) {
            log.at(Level.INFO).withCause(e).log("%s raised FORBIDDEN: %s", source, e.getMessage());
            return newResponse403(e);
        } catch (NotFoundException e) {
            log.at(Level.INFO).withCause(e).log("%s raised NOT_FOUND: %s", source, e.getMessage());
            return newResponse404(e);
        } catch (RedirectException e) {
            log.at(Level.INFO).withCause(e).log("%s raised REDIRECT: %s (%s): %s",
                   source, e.uri(), e.isPermanent() ? "permanent" : "temporary", e.getMessage());
            return newResponseRedirect(e.uri(), e.isPermanent());
        } catch (ServiceUnavailableException e) {
            log.at(Level.WARNING).withCause(e).log("%s raised NOT_AVAILABLE: %s", source, e.getMessage());
            return newResponse503("%s raised NOT_AVAILABLE".formatted(source), e);
        } catch (HttpCodeException e) {
            log.at(Level.WARNING).withCause(e).log("%s raised HttpCodeException(%s): %s", source, e.getStatus(), e.getMessage());
            return newErrorResponse(e.getStatus(), null, e);
        } catch (ServeException e) {
            return newErrorResponse(HttpResponseStatus.NOT_ACCEPTABLE, null, e);
        }
    }

    private static @NotNull String statusLine(@NotNull HttpResponseStatus status) {
        return "%d %s".formatted(status.code(), status.codeAsText());
    }

    private @NotNull ByteBuf getDefaultResource(@NotNull String name) throws IOException {
        String resource = "%s/%s".formatted(DEFAULT_WEB, name);
        InputStream inputStream = getClass().getResourceAsStream(resource);
        if (inputStream == null) {
            throw new FileNotFoundException("The resource %s is not found in classpath".formatted(resource));
        }
        return Unpooled.wrappedBuffer(inputStream.readAllBytes());
    }

    private static <R extends HttpResponse> @NotNull R withContentType(@NotNull R response, @NotNull CharSequence contentType) {
        response.headers().set(HttpConst.CONTENT_TYPE, contentType);
        return response;
    }

    public static <R extends HttpResponse> @NotNull R withHeaders(@NotNull R response, @NotNull Consumer<HttpHeaders> consumer) {
        HttpHeaders headers = response.headers();
        consumer.accept(headers);
        return response;
    }
}
