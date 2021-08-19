package io.webby.netty.marshal;

import com.dslplatform.json.DslJson;
import com.google.inject.Inject;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Objects;

import static io.webby.util.Rethrow.rethrow;

public record DslJsonMarshaller(@NotNull DslJson<Object> json, @NotNull Charset charset) implements Json, Marshaller {
    @Inject
    public DslJsonMarshaller(@NotNull DslJson<Object> json, @NotNull Charset charset) {
        this.json = json;
        this.charset = charset;
    }

    @Override
    public @NotNull Marshaller withCustomCharset(@NotNull Charset charset) {
        return charset == this.charset ? this : new DslJsonMarshaller(json, charset);
    }

    @Override
    public void writeBytes(@NotNull OutputStream output, @NotNull Object instance) throws IOException {
        json.serialize(instance, output);
    }

    @Override
    public <T> @NotNull T readBytes(@NotNull InputStream input, @NotNull Class<T> klass) throws IOException {
        return Objects.requireNonNull(json.deserialize(klass, input));
    }

    @Override
    public <T> @NotNull T readBytes(byte @NotNull [] bytes, @NotNull Class<T> klass) {
        try {
            return Objects.requireNonNull(json.deserialize(klass, bytes, bytes.length));
        } catch (IOException e) {
            return rethrow(e);
        }
    }
}
