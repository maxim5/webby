package io.webby.netty.response;

import com.google.common.flogger.FluentLogger;
import com.google.inject.Inject;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import io.webby.app.Settings;
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
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Level;

public class StaticServing {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    @Inject private Settings settings;
    @Inject private HttpResponseFactory factory;

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
        Path fullPath = settings.webPath().resolve(path);

        ByteBuf byteBuf = getByteBufOrNull(fullPath);
        if (byteBuf == null) {
            log.at(Level.WARNING).log("Can't serve the static path: %s. Return 404", path);
            return factory.newResponse404();
        }

        CharSequence contentType = guessContentType(fullPath);
        if (contentType == null) {
            contentType = HttpHeaderValues.APPLICATION_OCTET_STREAM;
        }

        FullHttpResponse response = factory.newResponse(byteBuf, HttpResponseStatus.OK, contentType);
        response.headers().add(HttpHeaderNames.CACHE_CONTROL, "max-age=31536000");

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

    // Consider also
    // https://github.com/j256/simplemagic (Unix)
    // https://github.com/oblac/jodd/
    // https://github.com/arimus/jmimemagic
    // org.apache.tika
    private static final Map<String, CharSequence> EXTENSIONS = Map.of(
        "js", "application/javascript"
    );

    @VisibleForTesting
    static @Nullable CharSequence guessContentType(@NotNull Path path) throws IOException {
        String fullName = path.toString();
        CharSequence knownType = EXTENSIONS.get(com.google.common.io.Files.getFileExtension(fullName));
        if (knownType != null) {
            return knownType;
        }
        String contentType = URLConnection.guessContentTypeFromName(fullName);
        if (contentType != null) {
            return contentType;
        }
        return Files.probeContentType(path);
    }
}
