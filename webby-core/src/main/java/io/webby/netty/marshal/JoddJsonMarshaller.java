package io.webby.netty.marshal;

import com.google.common.io.CharStreams;
import com.google.inject.Inject;
import io.webby.common.InjectorHelper;
import jodd.json.JsonParser;
import jodd.json.JsonSerializer;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.charset.Charset;

public record JoddJsonMarshaller(@NotNull JsonSerializer serializer, @NotNull Charset charset) implements Json, Marshaller {
    @Inject
    public JoddJsonMarshaller(@NotNull InjectorHelper helper, @NotNull Charset charset) {
        this(helper.getOrDefault(JsonSerializer.class, JoddJsonMarshaller::defaultJoddSerializer), charset);
    }

    @Override
    public @NotNull Marshaller withCustomCharset(@NotNull Charset charset) {
        return new JoddJsonMarshaller(serializer, charset);
    }

    @Override
    public void writeChars(@NotNull Writer writer, @NotNull Object instance) throws IOException {
        serializer.serialize(instance, writer);
    }

    @Override
    public @NotNull String writeString(@NotNull Object instance) {
        return serializer.serialize(instance);
    }

    @Override
    public <T> @NotNull T readChars(@NotNull Reader reader, @NotNull Class<T> klass) throws IOException {
        return readString(CharStreams.toString(reader), klass);
    }

    @Override
    public <T> @NotNull T readString(@NotNull String str, @NotNull Class<T> klass) {
        return JsonParser.create().parse(str, klass);
    }

    private static @NotNull JsonSerializer defaultJoddSerializer() {
        return JsonSerializer.create()
                .deep(true);
    }
}
