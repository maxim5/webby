package io.webby.netty.marshal;

import org.jetbrains.annotations.NotNull;

import java.io.*;

public interface TextMarshaller {
    void writeChars(@NotNull Writer writer, @NotNull Object instance) throws IOException;

    default @NotNull String writeString(@NotNull Object instance) throws IOException {
        try (StringWriter writer = new StringWriter()) {
            writeChars(writer, instance);
            return writer.toString();
        }
    }

    <T> @NotNull T readChars(@NotNull Reader reader, @NotNull Class<T> klass) throws IOException;

    default <T> @NotNull T readString(@NotNull String str, @NotNull Class<T> klass) throws IOException {
        return readChars(new StringReader(str), klass);
    }
}
