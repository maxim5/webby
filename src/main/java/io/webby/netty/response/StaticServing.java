package io.webby.netty.response;

import com.google.common.flogger.FluentLogger;
import com.google.inject.Inject;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import io.webby.app.Settings;
import io.webby.common.InjectorHelper;
import io.webby.netty.HttpConst;
import io.webby.util.Rethrow;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;

import static io.webby.util.EasyObjects.firstNonNull;

public class StaticServing {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    private ContentTypeProvider contentTypeProvider;
    @Inject private Settings settings;
    @Inject private HttpResponseFactory factory;

    @Inject
    private void init(@NotNull InjectorHelper helper) {
        contentTypeProvider = helper.getOrDefault(ContentTypeProvider.class, () -> path -> null);
    }

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

        ByteBuf byteBuf = getByteBufOrNull(fullPath);
        if (byteBuf == null) {
            log.at(Level.WARNING).log("Can't serve the static path: %s. Return 404", path);
            return factory.newResponse404();
        }
        if (!HttpCaching.isSimpleEtagChanged(byteBuf, headers.get(HttpConst.IF_NONE_MATCH))) {
            return factory.newResponse304();
        }

        FullHttpResponse response = factory.newResponse(byteBuf, HttpResponseStatus.OK, guessContentType(fullPath));
        if (settings.isProdMode()) {
            response.headers().add(HttpConst.CACHE_CONTROL, HttpCaching.CACHE_FOREVER);
        } else {
            response.headers().add(HttpConst.CONTENT_DISPOSITION, HttpConst.INLINE);
            response.headers().add(HttpConst.ETAG, HttpCaching.simpleEtag(byteBuf));
            response.headers().add(HttpConst.LAST_MODIFIED, HttpCaching.lastModified(fullPath));
        }

        return response;
    }

    /*package*/ @Nullable ByteBuf getByteBufOrNull(@NotNull String path) throws IOException {
        return getByteBufOrNull(settings.webPath().resolve(path));
    }

    /*package*/ @Nullable ByteBuf getByteBufOrNull(@NotNull Path path) throws IOException {
        if (Files.exists(path)) {
            return fileToByteBuf(path.toFile());
        }
        return null;
    }

    /*package*/ byte @Nullable [] getBytesOrNull(@NotNull Path path) throws IOException {
        if (Files.exists(path)) {
            return Files.readAllBytes(path);
        }
        return null;
    }

    private static @NotNull ByteBuf fileToByteBuf(@NotNull File file) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(file);
        FileChannel fileChannel = fileInputStream.getChannel();
        MappedByteBuffer mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, file.length());
        return Unpooled.wrappedBuffer(mappedByteBuffer);
    }

    @VisibleForTesting
    CharSequence guessContentType(@NotNull Path path) {
        return firstNonNull(List.of(
            () -> contentTypeProvider.getContentType(path),
            () -> URLConnection.guessContentTypeFromName(path.toString()),
            Rethrow.Suppliers.rethrow(() -> Files.probeContentType(path)),
            Rethrow.Suppliers.rethrow(() -> ThirdPartyMimeTypeDetectors.detect(path.toFile()))
        ), HttpHeaderValues.APPLICATION_OCTET_STREAM);
    }
}
