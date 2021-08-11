package io.webby.netty.marshal;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.charset.Charset;

public interface Marshaller {
    void writeChars(@NotNull Writer writer, @NotNull Object instance) throws IOException;

    default void writeBytes(@NotNull OutputStream output, @NotNull Object instance, @NotNull Charset charset) throws IOException {
        writeChars(new OutputStreamWriter(output, charset), instance);
    }

    default byte @NotNull [] writeBytes(@NotNull Object instance, @NotNull Charset charset) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        writeBytes(output, instance, charset);
        return output.toByteArray();
    }

    default @NotNull ByteBuf writeByteBuf(@NotNull Object instance, @NotNull Charset charset) throws IOException {
        return Unpooled.wrappedBuffer(writeBytes(instance, charset));
    }

    <T> @NotNull T readChars(@NotNull Reader reader, @NotNull Class<T> klass) throws IOException;

    default <T> @NotNull T readBytes(@NotNull InputStream input, @NotNull Class<T> klass, @NotNull Charset charset) throws IOException {
        return readChars(new InputStreamReader(input, charset), klass);
    }

    default <T> @NotNull T readBytes(byte @NotNull [] bytes, @NotNull Class<T> klass, @NotNull Charset charset) throws IOException {
        return readBytes(new ByteArrayInputStream(bytes), klass, charset);
    }

    default <T> @NotNull T readByteBuf(@NotNull ByteBuf byteBuf, @NotNull Class<T> klass, @NotNull Charset charset) throws IOException {
        return readBytes(new ByteBufInputStream(byteBuf), klass, charset);
    }
}
