package io.webby.testing;

import io.netty.buffer.ByteBuf;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import io.webby.netty.request.DefaultHttpRequestEx;
import io.webby.netty.request.HttpRequestEx;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class FakeRequests {
    public static @NotNull FullHttpRequest get(@NotNull String uri) {
        return request(HttpMethod.GET, uri, null);
    }

    public static @NotNull FullHttpRequest post(@NotNull String uri) {
        return request(HttpMethod.POST, uri, null);
    }

    public static @NotNull FullHttpRequest post(@NotNull String uri, @NotNull Object content) {
        return request(HttpMethod.POST, uri, content);
    }

    public static @NotNull FullHttpRequest request(@NotNull HttpMethod method, @NotNull String uri, @Nullable Object content) {
        ByteBuf byteBuf = TestingBytes.asByteBuf((content instanceof String str) ? str : toJson(content));
        return new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, method, uri, byteBuf);
    }

    public static @NotNull HttpRequestEx getEx(@NotNull String uri) {
        return requestEx(get(uri));
    }

    public static @NotNull HttpRequestEx postEx(@NotNull String uri) {
        return requestEx(post(uri));
    }

    public static @NotNull HttpRequestEx requestEx(@NotNull FullHttpRequest request) {
        return new DefaultHttpRequestEx(request, new EmbeddedChannel(), Testing.Internals.json(), Map.of(), new Object[0]);
    }

    public static @Nullable String toJson(@Nullable Object obj) {
        return obj != null ? Testing.Internals.json().writeString(obj) : null;
    }
}
