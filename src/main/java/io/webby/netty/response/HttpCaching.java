package io.webby.netty.response;

import com.google.common.flogger.FluentLogger;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Level;

public class HttpCaching {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    public static final String CACHE_FOREVER = "max-age=31536000";
    public static final DateFormat DATE_FORMAT = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH);

    public static @NotNull String lastModified(@NotNull Path path) throws IOException {
        FileTime modifiedTime = Files.getLastModifiedTime(path);
        return DATE_FORMAT.format(modifiedTime.toMillis());
    }

    public static boolean isModifiedSince(@NotNull Path path, @Nullable String ifModifiedSince) {
        try {
            if (ifModifiedSince != null) {
                Date timestamp = DATE_FORMAT.parse(ifModifiedSince); // If-Modified-Since: Fri, 27 Aug 2021 22:46:13 CEST
                FileTime modifiedTime = Files.getLastModifiedTime(path);
                return modifiedTime.toInstant().isAfter(timestamp.toInstant());
            }
        } catch (ParseException | IOException e) {
            log.at(Level.INFO).withCause(e).log("Failed to check modified: path=%s header=%s", path, ifModifiedSince);
        }
        return true;
    }

    public static @NotNull String simpleEtag(@NotNull ByteBuf byteBuf) {
        return "%08x".formatted(ByteBufUtil.hashCode(byteBuf));
    }

    public static boolean isSimpleEtagChanged(@NotNull ByteBuf byteBuf, @Nullable String ifNoneMatch) {
        return ifNoneMatch == null || !simpleEtag(byteBuf).equals(ifNoneMatch);  // If-None-Match: 1839ce40
    }
}
