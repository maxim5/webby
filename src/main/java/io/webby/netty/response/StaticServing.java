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
import java.nio.file.Paths;
import java.util.function.Consumer;
import java.util.logging.Level;

public class StaticServing {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    @Inject private Settings settings;
    @Inject private HttpResponseFactory factory;

    public void iterateStaticFiles(@NotNull Consumer<String> consumer) throws IOException {
        Path webPath = Paths.get(settings.webPath());
        Files.walk(webPath).forEach(path -> {
            if (path.toFile().isFile()) {
                consumer.accept(webPath.relativize(path).toString());
            }
        });
    }

    public boolean accept(@NotNull HttpMethod method) {
        return method.equals(HttpMethod.GET);
    }

    @NotNull
    public FullHttpResponse serve(@NotNull String path, @NotNull FullHttpRequest request) throws IOException {
        ByteBuf byteBuf = getByteBufOrNull(path);
        if (byteBuf == null) {
            log.at(Level.WARNING).log("Can't serve the static path: %s. Return 404", path);
            return factory.newResponse404();
        }

        CharSequence contentType = guessContentType(path);
        if (contentType == null) {
            contentType = HttpHeaderValues.APPLICATION_OCTET_STREAM;
        }

        FullHttpResponse response = factory.newResponse(byteBuf, HttpResponseStatus.OK, contentType);
        response.headers().add(HttpHeaderNames.CACHE_CONTROL, "max-age=31536000");

        return response;
    }

    @Nullable
    /*package*/ ByteBuf getByteBufOrNull(@NotNull String path) throws IOException {
        File file = new File(settings.webPath(), path);
        if (file.exists()) {
            return fileToByteBuf(file);
        }
        return null;
    }

    /*package*/ byte[] getBytesOrNull(@NotNull String path) throws IOException {
        File file = new File(settings.webPath(), path);
        if (file.exists()) {
            return Files.readAllBytes(file.toPath());
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

    @VisibleForTesting
    static CharSequence guessContentType(@NotNull String path) throws IOException {
        String contentType = URLConnection.guessContentTypeFromName(path);
        if (contentType != null) {
            return contentType;
        }
        return Files.probeContentType(Paths.get(path));
    }
}
