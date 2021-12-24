package io.webby.netty.response;

import com.google.common.flogger.FluentLogger;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import io.webby.app.Settings;
import io.webby.netty.HttpConst;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;

public class HttpCachingRequestProcessor {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();
    private static final int MAX_IN_MEMORY_CONTENT_SIZE = 1 << 20;

    private final Settings settings;
    private final HttpResponseFactory factory;
    private final ContentProvider provider;

    public HttpCachingRequestProcessor(@NotNull Settings settings,
                                       @NotNull HttpResponseFactory factory,
                                       @NotNull ContentProvider provider) {
        this.provider = provider;
        this.settings = settings;
        this.factory = factory;
    }

    public @NotNull HttpResponse process(@NotNull String path, @NotNull HttpRequest request) throws IOException {
        HttpHeaders headers = request.headers();

        if (!provider.exists()) {
            log.at(Level.WARNING).log("Requested path does not exist: %s. Return 404", path);
            return factory.newResponse404();
        }

        long lastModifiedMillis = provider.getLastModifiedMillis();
        String ifModifiedSince = headers.get(HttpConst.IF_MODIFIED_SINCE);
        if (ifModifiedSince != null && !HttpCaching.isModifiedSince(lastModifiedMillis, ifModifiedSince)) {
            return factory.newResponse304();
        }

        long sizeInBytes = provider.getFileSizeInBytes();
        CharSequence contentType = provider.getContentType();

        if (sizeInBytes >= MAX_IN_MEMORY_CONTENT_SIZE) {
            StreamingHttpResponse response = factory.newResponse(provider.readFileContent(), HttpResponseStatus.OK, contentType);
            response.headers().set(HttpConst.CONTENT_LENGTH, sizeInBytes);
            addCachingHeaders(response.headers(), lastModifiedMillis, null);
            return response;
        }

        ByteBuf byteBuf = Unpooled.wrappedBuffer(provider.getFileContent());
        if (!HttpCaching.isSimpleEtagChanged(byteBuf, headers.get(HttpConst.IF_NONE_MATCH))) {
            return factory.newResponse304();
        }

        FullHttpResponse response = factory.newResponse(byteBuf, HttpResponseStatus.OK, contentType);
        response.headers().add(HttpConst.CONTENT_DISPOSITION, HttpConst.INLINE);
        addCachingHeaders(response.headers(), lastModifiedMillis, byteBuf);

        return response;
    }

    private void addCachingHeaders(@NotNull HttpHeaders headers, long lastModifiedMillis, @Nullable ByteBuf byteBuf) {
        if (settings.isProdMode()) {
            headers.add(HttpConst.CACHE_CONTROL, HttpCaching.CACHE_FOREVER);
        } else {
            headers.add(HttpConst.LAST_MODIFIED, HttpCaching.lastModifiedValue(lastModifiedMillis));
            if (byteBuf != null) {
                headers.add(HttpConst.ETAG, HttpCaching.simpleEtag(byteBuf));
            }
        }
    }

    interface ContentProvider {
        boolean exists() throws IOException;

        long getFileSizeInBytes() throws IOException;

        long getLastModifiedMillis() throws IOException;

        CharSequence getContentType();

        InputStream readFileContent() throws IOException;

        default byte[] getFileContent() throws IOException {
            try (InputStream stream = readFileContent()) {
                return stream.readAllBytes();
            }
        }
    }
}
