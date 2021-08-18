package io.webby.netty.marshal;

import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.charset.Charset;

public interface Marshaller extends BinaryMarshaller, TextMarshaller {
    @NotNull Charset charset();

    @NotNull Marshaller withCustomCharset(@NotNull Charset charset);

    default void writeBytes(@NotNull OutputStream output, @NotNull Object instance) throws IOException {
        try (OutputStreamWriter writer = new OutputStreamWriter(output, charset())) {
            writeChars(writer, instance);
        }
    }

    default <T> @NotNull T readBytes(@NotNull InputStream input, @NotNull Class<T> klass) throws IOException {
        try (InputStreamReader reader = new InputStreamReader(input, charset())) {
            return readChars(reader, klass);
        }
    }
}
