package io.spbx.webby.netty.response;

import com.google.common.flogger.FluentLogger;
import com.google.inject.Inject;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import io.spbx.util.io.EasyFiles;
import io.spbx.webby.app.Settings;
import io.spbx.webby.netty.HttpConst;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;

public class HttpCachingRequestProcessor {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();
    private static final int MAX_IN_MEMORY_CONTENT_SIZE = 1 << 20;

    @Inject private Settings settings;
    @Inject private HttpResponseFactory factory;
    @Inject private ContentTypeDetector detector;

    public @NotNull HttpResponse process(@NotNull Path path, @NotNull HttpRequest request) throws IOException {
        HttpHeaders headers = request.headers();

        if (!Files.exists(path)) {
            log.at(Level.WARNING).log("Requested path does not exist: %s. Return 404", path);
            return factory.newResponse404();
        }

        long lastModifiedMillis = EasyFiles.getLastModifiedTime(path);
        if (!HttpCaching.isModifiedSince(lastModifiedMillis, headers)) {
            return factory.newResponse304();
        }

        long sizeInBytes = Files.size(path);
        CharSequence contentType = detector.guessContentType(path);

        if (sizeInBytes >= MAX_IN_MEMORY_CONTENT_SIZE) {
            FileInputStream stream = new FileInputStream(path.toFile());
            StreamingHttpResponse response = factory.newResponse(stream, HttpResponseStatus.OK, contentType);
            response.headers().set(HttpConst.CONTENT_LENGTH, sizeInBytes);
            addCachingHeaders(response.headers(), lastModifiedMillis, null);
            return response;
        }

        ByteBuf byteBuf = Unpooled.wrappedBuffer(Files.readAllBytes(path));
        if (!HttpCaching.isSimpleEtagChanged(byteBuf, headers)) {
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
            headers.add(HttpConst.CACHE_CONTROL, HttpCaching.NO_NOT_CACHE);
            headers.add(HttpConst.LAST_MODIFIED, HttpCaching.lastModifiedValue(lastModifiedMillis));
            if (byteBuf != null) {
                headers.add(HttpConst.ETAG, HttpCaching.simpleEtag(byteBuf));
            }
        }
    }
}
