package io.spbx.webby.netty.response;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.handler.codec.DateFormatter;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.util.AsciiString;
import io.spbx.webby.netty.HttpConst;
import org.jetbrains.annotations.NotNull;

import java.sql.Timestamp;

// Date format: RFC_1123_DATE_TIME "EEE, dd MMM yyyy HH:mm:ss zzz"
public class HttpCaching {
    public static final AsciiString CACHE_FOREVER = AsciiString.of("max-age=31536000");
    public static final AsciiString NO_NOT_CACHE = AsciiString.of("max-age=0");

    public static @NotNull String lastModifiedValue(long millis) {
        return DateFormatter.format(new Timestamp(millis));
    }

    public static boolean isModifiedSince(long modifiedTime, @NotNull HttpHeaders headers) {
        // If-Modified-Since: Fri, 27 Aug 2021 22:46:13 GMT
        Long resourceMillis = headers.getTimeMillis(HttpConst.IF_MODIFIED_SINCE);
        return resourceMillis == null || modifiedTime > resourceMillis + 1000;  // one extra second
    }

    public static boolean isSimpleEtagChanged(@NotNull ByteBuf byteBuf, HttpHeaders headers) {
        String ifNoneMatch = headers.get(HttpConst.IF_NONE_MATCH);
        return ifNoneMatch == null || !simpleEtag(byteBuf).equals(ifNoneMatch);  // If-None-Match: 1839ce40
    }

    public static @NotNull String simpleEtag(@NotNull ByteBuf byteBuf) {
        return "%08x".formatted(ByteBufUtil.hashCode(byteBuf));
    }
}
