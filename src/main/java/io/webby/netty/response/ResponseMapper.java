package io.webby.netty.response;

import com.google.common.io.CharStreams;
import com.google.inject.Inject;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.routekit.util.CharBuffer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static io.webby.url.view.RenderUtil.castAny;
import static io.webby.url.view.RenderUtil.closeQuietly;

public class ResponseMapper {
    private static final Charset charset = Charset.defaultCharset();        // TODO: make a setting

    private final HttpResponseFactory factory;
    private final Map<Class<?>, Function<?, FullHttpResponse>> classMap = new HashMap<>();
    private final Map<Class<?>, Function<?, FullHttpResponse>> interfaceMap = new HashMap<>();

    @Inject
    public ResponseMapper(@NotNull HttpResponseFactory factory) {
        this.factory = factory;

        add(byte[].class, bytes -> respond(Unpooled.wrappedBuffer(bytes)));
        add(ByteBuf.class, this::respond);
        add(ByteBuffer.class, buffer -> respond(Unpooled.wrappedBuffer(buffer)));
        add(ReadableByteChannel.class, byteChannel -> respond(Channels.newInputStream(byteChannel)));

        add(char[].class, chars -> respond(new CharBuffer(chars)));
        add(java.nio.CharBuffer.class, buffer ->
                buffer.hasArray() ?
                        respond(Unpooled.copiedBuffer(buffer.array(), charset)) :
                        respond(buffer.toString()));

        add(CharSequence.class, this::respond);
        add(String.class, this::respond);
        add(CharBuffer.class, this::respond);

        add(InputStream.class, this::respond);
        add(Readable.class, readable -> respond(readToString(readable)));
        add(Reader.class, reader -> respond(readToString(reader)));
    }

    @NotNull
    private String readToString(Readable readable) {
        try {
            return CharStreams.toString(readable);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (readable instanceof Closeable closeable) {
                closeQuietly(closeable);
            }
        }
    }

    @NotNull
    private FullHttpResponse respond(ByteBuf byteBuf) {
        return factory.newResponse(byteBuf, HttpResponseStatus.OK);
    }

    @NotNull
    private FullHttpResponse respond(InputStream stream) {
        return factory.newResponse(stream, HttpResponseStatus.OK, ""); // TODO: content type
    }

    @NotNull
    private FullHttpResponse respond(CharSequence string) {
        return factory.newResponse(string, HttpResponseStatus.OK);
    }

    private <B> void add(@NotNull Class<B> key, @NotNull Function<B, FullHttpResponse> value) {
        if (key.isInterface()) {
            interfaceMap.put(key, value);
        } else {
            classMap.put(key, value);
        }
    }

    @Nullable
    public Function<Object, FullHttpResponse> mapInstance(Object object) {
        Class<?> klass = object.getClass();
        Function<Object, FullHttpResponse> result = lookupClass(klass);
        if (result != null) {
            return result;
        }

        for (Map.Entry<Class<?>, Function<?, FullHttpResponse>> entry : interfaceMap.entrySet()) {
            if (entry.getKey().isInstance(object)) {
                return castAny(entry.getValue());
            }
        }

        return null;
    }

    // https://stackoverflow.com/questions/32229528/inheritance-aware-class-to-value-map-to-replace-series-of-instanceof
    @Nullable
    public <T extends U, U> Function<U, FullHttpResponse> lookupClass(Class<T> clazz) {
        if (clazz == null) {
            return null;
        }
        Function<?, FullHttpResponse> value = classMap.get(clazz);
        if (value == null) {
            Class<?> superclass = clazz.getSuperclass();
            return castAny(lookupClass(superclass));
        }
        return castAny(value);
    }
}