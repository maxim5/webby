package io.spbx.webby.netty.marshal;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;
import io.spbx.util.func.Reversible;
import io.spbx.webby.app.AppSettings;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.ByteBuffer;

import static io.spbx.util.base.Unchecked.rethrow;
import static io.spbx.webby.app.Settings.SIZE_BYTES;

public interface BinaryMarshaller {
    void writeBytes(@NotNull OutputStream output, @NotNull Object instance) throws IOException;

    default byte @NotNull [] writeBytes(@NotNull Object instance) {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream(AppSettings.live().getInt(SIZE_BYTES))) {
            writeBytes(output, instance);
            return output.toByteArray();
        } catch (IOException impossible) {
            return rethrow(impossible);
        }
    }

    default @NotNull ByteBuffer writeByteBuffer(@NotNull Object instance) {
        return ByteBuffer.wrap(writeBytes(instance));
    }

    default @NotNull ByteBuf writeByteBuf(@NotNull Object instance) {
        return Unpooled.wrappedBuffer(writeBytes(instance));
    }

    <T> @NotNull T readBytes(@NotNull InputStream input, @NotNull Class<T> klass) throws IOException;

    default <T> @NotNull T readBytes(byte @NotNull [] bytes, @NotNull Class<T> klass) {
        try (ByteArrayInputStream input = new ByteArrayInputStream(bytes)) {
            return readBytes(input, klass);
        } catch (IOException impossible) {
            return rethrow(impossible);
        }
    }

    default <T> @NotNull T readFromByteBuffer(@NotNull ByteBuffer buffer, @NotNull Class<T> klass) {
        return readByteBuf(Unpooled.wrappedBuffer(buffer), klass);
    }

    default <T> @NotNull T readByteBuf(@NotNull ByteBuf byteBuf, @NotNull Class<T> klass) {
        try (ByteBufInputStream input = new ByteBufInputStream(byteBuf)) {
            return readBytes(input, klass);
        } catch (IOException impossible) {
            return rethrow(impossible);
        }
    }
    
    default <T> @NotNull Reversible<byte[], T> toBinaryReversible(@NotNull Class<T> klass) {
        return new Reversible<>() {
            @Override
            public @NotNull T forward(byte @NotNull [] bytes) {
                return readBytes(bytes, klass);
            }

            @Override
            public byte @NotNull [] backward(@NotNull T t) {
                return writeBytes(t);
            }
        };
    }
}
