package io.webby.netty.marshal;

import com.google.common.io.CharStreams;
import com.google.inject.Inject;
import com.squareup.moshi.Moshi;
import io.webby.common.InjectorHelper;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static io.webby.util.base.EasyCast.castAny;
import static java.util.Objects.requireNonNull;

public record MoshiMarshaller(@NotNull Moshi moshi, @NotNull Charset charset) implements Json {
    @Inject
    public MoshiMarshaller(@NotNull InjectorHelper helper, @NotNull Charset charset) {
        this(helper.getOrDefault(Moshi.class, MoshiMarshaller::defaultMoshi), charset);
    }

    @Override
    public @NotNull Marshaller withCustomCharset(@NotNull Charset charset) {
        return charset == this.charset ? this : new MoshiMarshaller(moshi, charset);
    }

    @Override
    public void writeChars(@NotNull Writer writer, @NotNull Object instance) throws IOException {
        Class<Object> type = castAny(instance.getClass());
        String json = moshi.adapter(type).toJson(instance);
        writer.write(json);
    }

    @Override
    public void writeBytes(@NotNull OutputStream output, @NotNull Object instance) throws IOException {
        try (BufferedSink buffer = Okio.buffer(Okio.sink(output))) {
            Class<Object> type = castAny(instance.getClass());
            moshi.adapter(type).toJson(buffer, instance);
        }
    }

    @Override
    public <T> @NotNull T readChars(@NotNull Reader reader, @NotNull Class<T> klass) throws IOException {
        String json = CharStreams.toString(reader);
        return requireNonNull(moshi.adapter(klass).fromJson(json));
    }

    @Override
    public <T> @NotNull T readBytes(@NotNull InputStream input, @NotNull Class<T> klass) throws IOException {
        try (BufferedSource buffer = Okio.buffer(Okio.source(input))) {
            return requireNonNull(moshi.adapter(klass).fromJson(buffer));
        }
    }

    private static @NotNull Moshi defaultMoshi() {
        return new Moshi.Builder().add((type, annotations, moshi) -> {
            if (type instanceof Class<?> klass) {
                if (List.class.isAssignableFrom(klass) && klass != List.class) {
                    return moshi.adapter(List.class);
                }
                if (Collection.class.isAssignableFrom(klass) && klass != Collection.class) {
                    return moshi.adapter(Collection.class);
                }
                if (Map.class.isAssignableFrom(klass) && klass != Map.class) {
                    return moshi.adapter(Map.class);
                }
            }
            return null;
        }).build();
    }
}
