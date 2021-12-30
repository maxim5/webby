package io.webby.auth;

import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CookieUtil {
    private static final ServerCookieEncoder ENCODER = ServerCookieEncoder.LAX;
    private static final ServerCookieDecoder DECODER = ServerCookieDecoder.LAX;

    public static @NotNull String encode(@NotNull Cookie cookie) {
        return ENCODER.encode(cookie);
    }

    public static @NotNull List<Cookie> decodeAll(@NotNull String cookieHeader) {
        return DECODER.decodeAll(cookieHeader);
    }
}
