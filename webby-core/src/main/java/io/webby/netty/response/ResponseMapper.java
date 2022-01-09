package io.webby.netty.response;

import com.google.common.io.CharStreams;
import com.google.inject.Inject;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.routekit.util.CharArray;
import io.webby.app.Settings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static io.webby.util.base.EasyCast.castAny;
import static io.webby.util.io.EasyIO.Close.closeRethrow;
import static io.webby.util.base.Unchecked.Functions.rethrow;

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

        add(char[].class, chars -> respond(new CharArray(chars)));
        add(CharBuffer.class, buf -> buf.hasArray() ? respond(asByteBuf(buf)) : respond(buf.toString()));

        add(CharSequence.class, this::respond);
        add(String.class, this::respond);
        add(CharArray.class, this::respond);

        add(InputStream.class, this::respond);
        add(Readable.class, readable -> respond(readToString(readable)));
        add(Reader.class, reader -> respond(readToString(reader)));
        add(File.class, rethrow(this::respond));
    }

    public @Nullable Function<Object, HttpResponse> mapInstance(@NotNull Object instance) {
        Class<?> klass = instance.getClass();
        Function<Object, HttpResponse> result = lookupClass(klass);
        if (result != null) {
            return result;
        }

        for (Map.Entry<Class<?>, Function<?, HttpResponse>> entry : interfaceMap.entrySet()) {
            if (entry.getKey().isInstance(instance)) {
                return castAny(entry.getValue());
            }
        }

        return null;
    }

    // https://stackoverflow.com/questions/32229528/inheritance-aware-class-to-value-map-to-replace-series-of-instanceof
    public <T extends U, U> @Nullable Function<U, HttpResponse> lookupClass(@Nullable Class<T> klass) {
        if (klass == null) {
            return null;
        }
        Function<?, HttpResponse> value = classMap.get(klass);
        if (value == null) {
            Class<?> superclass = klass.getSuperclass();
            return castAny(lookupClass(superclass));
        }
        return castAny(value);
    }

    private @NotNull FullHttpResponse respond(@NotNull ByteBuf byteBuf) {
        return factory.newResponse(byteBuf, HttpResponseStatus.OK);
    }

    private @NotNull FullHttpResponse respond(@NotNull CharSequence string) {
        return factory.newResponse(string, HttpResponseStatus.OK);
    }

    private @NotNull StreamingHttpResponse respond(@NotNull InputStream stream) {
        return factory.newResponse(stream, HttpResponseStatus.OK);
    }

    private @NotNull StreamingHttpResponse respond(@NotNull File file) throws IOException {
        return factory.newResponse(file, HttpResponseStatus.OK);
    }

    private <B> void add(@NotNull Class<B> key, @NotNull Function<B, HttpResponse> value) {
        if (key.isInterface()) {
            interfaceMap.put(key, value);
        } else {
            classMap.put(key, value);
        }
    }

    private @NotNull ByteBuf asByteBuf(CharBuffer buffer) {
        return Unpooled.copiedBuffer(
                buffer.array(),
                buffer.arrayOffset() + buffer.position(),
                buffer.arrayOffset() + buffer.limit(),
                settings.charset()
        );
    }

    private static @NotNull String readToString(@NotNull Readable readable) {
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
