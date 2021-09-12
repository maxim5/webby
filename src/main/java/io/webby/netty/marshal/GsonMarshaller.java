package io.webby.netty.marshal;

import com.google.gson.Gson;
import com.google.inject.Inject;
import org.jetbrains.annotations.NotNull;

import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;

public record GsonMarshaller(@NotNull Gson gson, @NotNull Charset charset) implements Json, Marshaller {
    @Inject
    public GsonMarshaller(@NotNull Gson gson, @NotNull Charset charset) {
        this.gson = gson;
        this.charset = charset;
    }

    @Override
    public @NotNull Marshaller withCustomCharset(@NotNull Charset charset) {
        return charset == this.charset ? this : new GsonMarshaller(gson, charset);
    }

    @Override
    public void writeChars(@NotNull Writer writer, @NotNull Object instance) {
        gson.toJson(instance, writer);
    }

    @Override
    public <T> @NotNull T readChars(@NotNull Reader reader, @NotNull Class<T> klass) {
        T obj = gson.fromJson(reader, klass);
        assert obj != null : "GSON failed to parse the instance of class `%s` from the reader".formatted(klass);
        return obj;
    }
}
