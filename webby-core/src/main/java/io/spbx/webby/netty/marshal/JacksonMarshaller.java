package io.spbx.webby.netty.marshal;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.charset.Charset;

public record JacksonMarshaller(@NotNull ObjectMapper mapper, @NotNull Charset charset) implements Json, Marshaller {
    @Inject
    public JacksonMarshaller(@NotNull ObjectMapper mapper, @NotNull Charset charset) {
        this.mapper = mapper;
        this.charset = charset;
    }

    @Override
    public @NotNull Marshaller withCustomCharset(@NotNull Charset charset) {
        return charset == this.charset ? this : new JacksonMarshaller(mapper, charset);
    }

    @Override
    public void writeChars(@NotNull Writer writer, @NotNull Object instance) throws IOException {
        mapper.writeValue(writer, instance);
    }

    @Override
    public void writeBytes(@NotNull OutputStream output, @NotNull Object instance) throws IOException {
        mapper.writeValue(output, instance);
    }

    @Override
    public <T> @NotNull T readChars(@NotNull Reader reader, @NotNull Class<T> klass) throws IOException {
        return mapper.readValue(reader, klass);
    }

    @Override
    public <T> @NotNull T readBytes(@NotNull InputStream input, @NotNull Class<T> klass) throws IOException {
        return mapper.readValue(input, klass);
    }
}
