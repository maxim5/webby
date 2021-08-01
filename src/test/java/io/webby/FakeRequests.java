package io.webby;

import com.google.gson.Gson;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.DuplicatedByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FakeRequests {
    @NotNull
    public static FullHttpRequest get(@NotNull String uri) {
        return request(HttpMethod.GET, uri, null);
    }

    @NotNull
    public static FullHttpRequest request(HttpMethod method, String uri, @Nullable Object content) {
        ByteBuf byteBuf = asByteBuf((content instanceof String str) ? str : toJson(content));
        return new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, method, uri, byteBuf);
    }

    @Nullable
    public static String toJson(@Nullable Object obj) {
        return new Gson().toJson(obj);
    }

    @NotNull
    public static ByteBuf asByteBuf(@Nullable String content) {
        return readable(content != null ? Unpooled.copiedBuffer(content, Testing.CHARSET) : Unpooled.buffer(0));
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
}
