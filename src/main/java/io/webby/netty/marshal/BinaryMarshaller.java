package io.webby.netty.marshal;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.charset.Charset;

public interface BinaryMarshaller {
    void writeBytes(@NotNull OutputStream output, @NotNull Object instance, @NotNull Charset charset) throws IOException;

    default byte @NotNull [] writeBytes(@NotNull Object instance, @NotNull Charset charset) throws IOException {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            writeBytes(output, instance, charset);
            return output.toByteArray();
        }
    }

    default @NotNull ByteBuf writeByteBuf(@NotNull Object instance, @NotNull Charset charset) throws IOException {
        return Unpooled.wrappedBuffer(writeBytes(instance, charset));
    }

    <T> @NotNull T readBytes(@NotNull InputStream input, @NotNull Class<T> klass, @NotNull Charset charset) throws IOException;

    default <T> @NotNull T readBytes(byte @NotNull [] bytes, @NotNull Class<T> klass, @NotNull Charset charset) throws IOException {
        try (ByteArrayInputStream input = new ByteArrayInputStream(bytes)) {
            return readBytes(input, klass, charset);
        }
    }

    default <T> @NotNull T readByteBuf(@NotNull ByteBuf byteBuf, @NotNull Class<T> klass, @NotNull Charset charset) throws IOException {
        try (ByteBufInputStream input = new ByteBufInputStream(byteBuf)) {
            return readBytes(input, klass, charset);
        }
    }
}
