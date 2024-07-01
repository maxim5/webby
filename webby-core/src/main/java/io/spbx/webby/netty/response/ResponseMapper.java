package io.spbx.webby.netty.response;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.CharStreams;
import com.google.inject.Inject;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.spbx.util.base.CharArray;
import io.spbx.util.collect.ClassMap;
import io.spbx.util.base.Unchecked;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.function.Function;

import static io.spbx.util.base.EasyCast.castAny;
import static io.spbx.util.base.Unchecked.Functions.rethrow;
import static io.spbx.util.io.EasyIo.Close.closeRethrow;

public class ResponseMapper {
    private final ClassMap<Function<?, HttpResponse>> classMap;
    private final ClassMap<Function<?, HttpResponse>> interfaceMap;
    private final HttpResponseFactory factory;
    private final Charset charset;

    @Inject
    public ResponseMapper(@NotNull HttpResponseFactory factory, @NotNull Charset charset) {
        Builder builder = initClassMaps();
        this.classMap = ClassMap.immutableOf(builder.buildClasses());
        this.interfaceMap = ClassMap.immutableOf(builder.buildInterfaces());
        this.factory = factory;
        this.charset = charset;
    }

    private @NotNull Builder initClassMaps() {
        Builder builder = new Builder();

        builder.map(byte[].class, bytes -> respond(Unpooled.wrappedBuffer(bytes)));
        builder.map(ByteBuf.class, this::respond);
        builder.map(ByteBuffer.class, buffer -> respond(Unpooled.wrappedBuffer(buffer)));
        builder.map(ReadableByteChannel.class, byteChannel -> respond(Channels.newInputStream(byteChannel)));

        builder.map(char[].class, chars -> respond(new CharArray(chars)));
        builder.map(CharBuffer.class, buf -> buf.hasArray() ? respond(asByteBuf(buf)) : respond(buf.toString()));

        builder.map(CharSequence.class, this::respond);
        builder.map(String.class, this::respond);
        builder.map(CharArray.class, this::respond);

        builder.map(InputStream.class, this::respond);
        builder.map(Readable.class, readable -> respond(readToString(readable)));
        builder.map(Reader.class, reader -> respond(readToString(reader)));
        builder.map(File.class, rethrow(this::respond));
        return builder;
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

    @VisibleForTesting
    <T extends U, U> @Nullable Function<U, HttpResponse> lookupClass(@NotNull Class<T> klass) {
        return castAny(classMap.getSuper(klass));
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

    private @NotNull ByteBuf asByteBuf(CharBuffer buffer) {
        return Unpooled.copiedBuffer(
            buffer.array(),
            buffer.arrayOffset() + buffer.position(),
            buffer.arrayOffset() + buffer.limit(),
            charset
        );
    }

    private static @NotNull String readToString(@NotNull Readable readable) {
        try {
            return CharStreams.toString(readable);
        } catch (IOException e) {
            return Unchecked.rethrow(e);
        } finally {
            if (readable instanceof Closeable closeable) {
                closeRethrow(closeable);
            }
        }
    }

    private static class Builder {
        private final ImmutableMap.Builder<Class<?>, Function<?, HttpResponse>> classes = new ImmutableMap.Builder<>();
        private final ImmutableMap.Builder<Class<?>, Function<?, HttpResponse>> interfaces = new ImmutableMap.Builder<>();

        public <B> void map(@NotNull Class<B> key, @NotNull Function<B, HttpResponse> value) {
            if (key.isInterface()) {
                interfaces.put(key, value);
            } else {
                classes.put(key, value);
            }
        }

        public @NotNull Map<Class<?>, Function<?, HttpResponse>> buildClasses() {
            return classes.buildOrThrow();
        }

        public @NotNull Map<Class<?>, Function<?, HttpResponse>> buildInterfaces() {
            return interfaces.buildOrThrow();
        }
    }
}
