package io.webby.netty.response;

import com.google.inject.Inject;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.routekit.util.CharBuffer;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static io.webby.url.view.RenderUtil.castAny;

public class ResponseMapper {
    private final HttpResponseFactory factory;
    private final Map<Class<?>, Function<?, FullHttpResponse>> map = new HashMap<>();

    @Inject
    public ResponseMapper(@NotNull HttpResponseFactory factory) {
        this.factory = factory;

        put(byte[].class, bytes -> respond(Unpooled.wrappedBuffer(bytes)));
        put(ByteBuf.class, this::respond);
        put(ByteBuffer.class, buffer -> respond(Unpooled.wrappedBuffer(buffer)));
        put(ReadableByteChannel.class, byteChannel -> respond(Channels.newInputStream(byteChannel)));

        put(char[].class, chars -> factory.newResponse(new CharBuffer(chars), HttpResponseStatus.OK));
        put(java.nio.CharBuffer.class, buffer -> respond(Unpooled.copiedBuffer(buffer.array(), Charset.defaultCharset())));

        put(CharSequence.class, charSequence -> factory.newResponse(charSequence, HttpResponseStatus.OK));
        put(String.class, str -> factory.newResponse(str, HttpResponseStatus.OK));

        put(InputStream.class, this::respond);
    }

    @NotNull
    private FullHttpResponse respond(ByteBuf byteBuf) {
        return factory.newResponse(byteBuf, HttpResponseStatus.OK);
    }

    @NotNull
    private FullHttpResponse respond(InputStream stream) {
        return factory.newResponse(stream, HttpResponseStatus.OK, ""); // TODO: content type
    }

    private <B> void put(@NotNull Class<B> key, @NotNull Function<B, FullHttpResponse> value) {
        map.put(key, value);
    }

    // https://stackoverflow.com/questions/32229528/inheritance-aware-class-to-value-map-to-replace-series-of-instanceof
    public <B> Function<B, FullHttpResponse> lookup(Class<? extends B> clazz) {
        if (clazz == null) {
            return null;
        }
        Function<?, FullHttpResponse> value = map.get(clazz);
        if (value == null) {
            Class<?> superclass = clazz.getSuperclass();
            return castAny(lookup(superclass));
        }
        return castAny(value);
    }
}
