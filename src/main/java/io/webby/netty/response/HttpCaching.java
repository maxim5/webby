package io.webby.netty.response;

import com.google.common.flogger.FluentLogger;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.util.AsciiString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Level;

public class HttpCaching {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    public static final AsciiString CACHE_FOREVER = AsciiString.of("max-age=31536000");
    public static final DateFormat DATE_FORMAT = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH);

    public static @NotNull String lastModifiedValue(long millis) {
        return DATE_FORMAT.format(millis);
    }

    public static boolean isModifiedSince(long modifiedTime, @NotNull String ifModifiedSince) {
        try {
            Date timestamp = DATE_FORMAT.parse(ifModifiedSince);  // If-Modified-Since: Fri, 27 Aug 2021 22:46:13 CET
            return Instant.ofEpochMilli(modifiedTime).isAfter(timestamp.toInstant());
        } catch (ParseException e) {
            log.at(Level.INFO).withCause(e).log("Failed to parse timestamp: header=%s", ifModifiedSince);
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
