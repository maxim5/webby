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
import java.util.Objects;

import static io.webby.util.EasyCast.castAny;

public record MoshiMarshaller(@NotNull Moshi moshi, @NotNull Charset charset) implements Json {
    public MoshiMarshaller(@NotNull Moshi moshi, @NotNull Charset charset) {
        this.moshi = moshi;
        this.charset = charset;
    }

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
        Class<Object> type = castAny(instance.getClass());
        BufferedSink buffer = Okio.buffer(Okio.sink(output));
        moshi.adapter(type).toJson(buffer, instance);
    }

    @Override
    public <T> @NotNull T readChars(@NotNull Reader reader, @NotNull Class<T> klass) throws IOException {
        String json = CharStreams.toString(reader);
        return Objects.requireNonNull(moshi.adapter(klass).fromJson(json));
    }

    @Override
    public <T> @NotNull T readBytes(@NotNull InputStream input, @NotNull Class<T> klass) throws IOException {
        BufferedSource buffer = Okio.buffer(Okio.source(input));
        return Objects.requireNonNull(moshi.adapter(klass).fromJson(buffer));
    }

    @NotNull
    private static Moshi defaultMoshi() {
        return new Moshi.Builder().build();
    }
}
