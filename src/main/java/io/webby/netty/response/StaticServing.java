package io.webby.netty.response;

import com.google.common.flogger.FluentLogger;
import com.google.inject.Inject;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.*;
import io.webby.app.Settings;
import io.webby.netty.HttpConst;
import io.webby.util.io.EasyFiles;
import io.webby.util.netty.EasyByteBuf;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.logging.Level;

public class StaticServing {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    @Inject private Settings settings;
    @Inject private HttpResponseFactory factory;
    @Inject private ContentTypeDetector detector;

    public void iterateStaticFiles(@NotNull Consumer<String> consumer) throws IOException {
        Path webPath = settings.webPath();
        Files.walk(webPath).forEach(path -> {
            if (path.toFile().isFile()) {
                consumer.accept(webPath.relativize(path).toString());
            }
        });
    }

    public boolean accept(@NotNull HttpMethod method) {
        return method.equals(HttpMethod.GET);
    }

    public @NotNull FullHttpResponse serve(@NotNull String path, @NotNull FullHttpRequest request) throws IOException {
        HttpHeaders headers = request.headers();
        Path fullPath = settings.webPath().resolve(path);
        if (!HttpCaching.isModifiedSince(fullPath, headers.get(HttpConst.IF_MODIFIED_SINCE))) {
            return factory.newResponse304();
        }

        ByteBuf byteBuf = EasyByteBuf.wrapNullable(EasyFiles.readByteBufferOrNull(fullPath));
        if (byteBuf == null) {
            log.at(Level.WARNING).log("Can't serve the static path: %s. Return 404", path);
            return factory.newResponse404();
        }
        if (!HttpCaching.isSimpleEtagChanged(byteBuf, headers.get(HttpConst.IF_NONE_MATCH))) {
            return factory.newResponse304();
        }

        FullHttpResponse response = factory.newResponse(byteBuf, HttpResponseStatus.OK, detector.guessContentType(fullPath));
        if (settings.isProdMode()) {
            response.headers().add(HttpConst.CACHE_CONTROL, HttpCaching.CACHE_FOREVER);
        } else {
            response.headers().add(HttpConst.CONTENT_DISPOSITION, HttpConst.INLINE);
            response.headers().add(HttpConst.ETAG, HttpCaching.simpleEtag(byteBuf));
            response.headers().add(HttpConst.LAST_MODIFIED, HttpCaching.lastModified(fullPath));
        }

        return response;
    }
}
