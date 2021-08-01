package io.webby.netty.response;

import com.google.common.io.CharStreams;
import com.google.inject.Inject;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.routekit.util.CharBuffer;
import io.webby.app.Settings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static io.webby.util.EasyCast.castAny;
import static io.webby.util.EasyIO.Close.closeRethrow;

public class ResponseMapper {
    private final Map<Class<?>, Function<?, HttpResponse>> classMap = new HashMap<>();
    private final Map<Class<?>, Function<?, HttpResponse>> interfaceMap = new HashMap<>();

    private final HttpResponseFactory factory;
    private final Settings settings;

    @Inject
    public ResponseMapper(@NotNull HttpResponseFactory factory, @NotNull Settings settings) {
        this.factory = factory;
        this.settings = settings;

        add(byte[].class, bytes -> respond(Unpooled.wrappedBuffer(bytes)));
        add(ByteBuf.class, this::respond);
        add(ByteBuffer.class, buffer -> respond(Unpooled.wrappedBuffer(buffer)));
        add(ReadableByteChannel.class, byteChannel -> respond(Channels.newInputStream(byteChannel)));

        add(char[].class, chars -> respond(new CharBuffer(chars)));
        add(java.nio.CharBuffer.class, buf -> buf.hasArray() ? respond(asByteBuf(buf)) : respond(buf.toString()));

        add(CharSequence.class, this::respond);
        add(String.class, this::respond);
        add(CharBuffer.class, this::respond);

        add(InputStream.class, this::respond);
        add(Readable.class, readable -> respond(readToString(readable)));
        add(Reader.class, reader -> respond(readToString(reader)));
    }

    @Nullable
    public Function<Object, HttpResponse> mapInstance(Object object) {
        Class<?> klass = object.getClass();
        Function<Object, HttpResponse> result = lookupClass(klass);
        if (result != null) {
            return result;
        }

        for (Map.Entry<Class<?>, Function<?, HttpResponse>> entry : interfaceMap.entrySet()) {
            if (entry.getKey().isInstance(object)) {
                return castAny(entry.getValue());
            }
        }

        return null;
    }

    // https://stackoverflow.com/questions/32229528/inheritance-aware-class-to-value-map-to-replace-series-of-instanceof
    @Nullable
    public <T extends U, U> Function<U, HttpResponse> lookupClass(Class<T> clazz) {
        if (clazz == null) {
            return null;
        }
        Function<?, HttpResponse> value = classMap.get(clazz);
        if (value == null) {
            Class<?> superclass = clazz.getSuperclass();
            return castAny(lookupClass(superclass));
        }
        return castAny(value);
    }

    @NotNull
    private FullHttpResponse respond(ByteBuf byteBuf) {
        return factory.newResponse(byteBuf, HttpResponseStatus.OK);
    }

    @NotNull
    private FullHttpResponse respond(InputStream stream) {
        return factory.newResponse(stream, HttpResponseStatus.OK);
    }

    @NotNull
    private FullHttpResponse respond(CharSequence string) {
        return factory.newResponse(string, HttpResponseStatus.OK);
    }

    private <B> void add(@NotNull Class<B> key, @NotNull Function<B, HttpResponse> value) {
        if (key.isInterface()) {
            interfaceMap.put(key, value);
        } else {
            classMap.put(key, value);
        }
    }

    @NotNull
    private ByteBuf asByteBuf(java.nio.CharBuffer buffer) {
        return Unpooled.copiedBuffer(
                buffer.array(),
                buffer.arrayOffset() + buffer.position(),
                buffer.arrayOffset() + buffer.limit(),
                settings.charset()
        );
    }

    @NotNull
    private static String readToString(Readable readable) {
        try {
            return CharStreams.toString(readable);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (readable instanceof Closeable closeable) {
                closeRethrow(closeable);
            }
        }
    }
}
