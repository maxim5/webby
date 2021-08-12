package io.webby.testing;

import com.google.gson.Gson;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.DuplicatedByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import io.webby.netty.marshal.JsonMarshaller;
import io.webby.netty.request.DefaultHttpRequestEx;
import io.webby.netty.request.HttpRequestEx;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;

import java.util.LinkedHashMap;
import java.util.List;
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

    public static @NotNull FullHttpRequest request(@NotNull HttpMethod method, String uri, @Nullable Object content) {
        ByteBuf byteBuf = asByteBuf((content instanceof String str) ? str : toJson(content));
        return new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, method, uri, byteBuf);
    }

    public static @NotNull HttpRequestEx getEx(@NotNull String uri) {
        return requestEx(get(uri));
    }

    public static @NotNull HttpRequestEx postEx(@NotNull String uri) {
        return requestEx(post(uri));
    }

    @NotNull
    public static HttpRequestEx requestEx(@NotNull FullHttpRequest request) {
        return new DefaultHttpRequestEx(request, new EmbeddedChannel(), new JsonMarshaller(), Map.of(), new Object[0]);
    }

    @Nullable
    public static String toJson(@Nullable Object obj) {
        return new Gson().toJson(obj);
    }

    @NotNull
    public static ByteBuf asByteBuf(@Nullable String content) {
        return readable(content != null ? Unpooled.copiedBuffer(content, Testing.CHARSET) : Unpooled.EMPTY_BUFFER);
    }

    public static ByteBuf readable(ByteBuf buf) {
        return new ReadableByteBuf(buf);
    }

    @SuppressWarnings("deprecation")
    protected static class ReadableByteBuf extends DuplicatedByteBuf {
        public ReadableByteBuf(ByteBuf buffer) {
            super(buffer);
        }

        @Override
        public String toString() {
            return unwrap().toString(Testing.CHARSET);
        }
    }

    public static <K, V> Map<K, V> asMap(Object ... items) {
        return asMap(List.of(items));
    }

    @SuppressWarnings("unchecked")
    public static <K, V> Map<K, V> asMap(List<?> items) {
        Assertions.assertEquals(0, items.size() % 2);
        LinkedHashMap<K, V> result = new LinkedHashMap<>();
        for (int i = 0; i < items.size(); i += 2) {
            result.put((K) items.get(i), (V) items.get(i + 1));
        }
        return result;
    }
}
