package io.webby.netty.response;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class HttpCaching {
    public static final String CACHE_FOREVER = "max-age=31536000";
    public static final DateFormat DATE_FORMAT = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");

    public static @NotNull String simpleEtag(@NotNull ByteBuf byteBuf) {
        return "%08x".formatted(ByteBufUtil.hashCode(byteBuf));
    }

    public static @NotNull String lastModified(@NotNull Path path) throws IOException {
        FileTime modifiedTime = Files.getLastModifiedTime(path);
        return DATE_FORMAT.format(modifiedTime.toMillis());
    }
}
