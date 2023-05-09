package io.webby.testing;

import io.netty.buffer.ByteBuf;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.*;
import io.webby.netty.request.DefaultHttpRequestEx;
import io.webby.url.convert.Constraint;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class HttpRequestBuilder {
    private final HttpMethod method;
    private final String uri;
    private HttpVersion version = HttpVersion.HTTP_1_1;
    private Object content = null;
    private HttpHeaders headers = new DefaultHttpHeaders();

    private final Map<String, Constraint<?>> constraints = Map.of();
    private int attributesSize = 0;

    public HttpRequestBuilder(@NotNull HttpMethod method, @NotNull String uri) {
        this.method = method;
        this.uri = uri;
    }

    public static @NotNull HttpRequestBuilder request(@NotNull HttpMethod method, @NotNull String uri) {
        return new HttpRequestBuilder(method, uri);
    }

    public static @NotNull HttpRequestBuilder get(@NotNull String uri) {
        return request(HttpMethod.GET, uri);
    }

    public static @NotNull HttpRequestBuilder post(@NotNull String uri) {
        return request(HttpMethod.POST, uri);
    }

    public @NotNull HttpRequestBuilder withVersion(@NotNull HttpVersion version) {
        this.version = version;
        return this;
    }

    public @NotNull HttpRequestBuilder withContent(@Nullable Object content) {
        this.content = content;
        return this;
    }

    public @NotNull HttpRequestBuilder withHeaders(@NotNull HttpHeaders headers) {
        this.headers = headers;
        return this;
    }

    public @NotNull HttpRequestBuilder allocate(int attributesSize) {
        this.attributesSize = attributesSize;
        return this;
    }

    public @NotNull FullHttpRequest full() {
        return new DefaultFullHttpRequest(version, method, uri, toByteBufContent(content), headers, headers);
    }

    public @NotNull DefaultHttpRequestEx ex() {
        return new DefaultHttpRequestEx(
            full(), new EmbeddedChannel(), Testing.Internals.json(), constraints, new Object[attributesSize]
        );
    }

    public static @NotNull ByteBuf toByteBufContent(@Nullable Object content) {
        if (content instanceof ByteBuf buf) {
            return buf;
        }
        if (content instanceof byte[] array) {
            return TestingBytes.asByteBufOrNull(array);
        }
        if (content instanceof String str) {
            return TestingBytes.asByteBuf(str);
        }
        return TestingBytes.asByteBuf(toJson(content));
    }

    public static @Nullable String toJson(@Nullable Object obj) {
        return obj != null ? Testing.Internals.json().writeString(obj) : null;
    }
}
