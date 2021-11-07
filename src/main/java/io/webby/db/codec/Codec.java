package io.webby.db.codec;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;
import io.webby.common.SystemProperties;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.ByteBuffer;

import static io.webby.util.base.EasyPrimitives.firstNonNegative;
import static io.webby.util.base.Rethrow.rethrow;

// Aka Serializer
public interface Codec<T> {
    @NotNull CodecSize size();

    default int sizeOf(@NotNull T instance) {
        CodecSize size = size();
        return size.isFixed() ? size.numBytes() : - 1;
    }

    @CanIgnoreReturnValue
    int writeTo(@NotNull OutputStream output, @NotNull T instance) throws IOException;

    default byte @NotNull [] writeToBytes(@NotNull T instance) {
        int expectedSize = firstNonNegative(sizeOf(instance), size().numBytes(), SystemProperties.DEFAULT_SIZE_BYTES);
        try (ByteArrayOutputStream output = new ByteArrayOutputStream(expectedSize)) {
            writeTo(output, instance);
            return output.toByteArray();
        } catch (IOException impossible) {
            return rethrow(impossible);
        }
    }

    default byte @NotNull [] writeToBytes(byte @NotNull [] prefix, @NotNull T instance) {
        int expectedSize = firstNonNegative(sizeOf(instance), size().numBytes(), SystemProperties.DEFAULT_SIZE_BYTES);
        try (ByteArrayOutputStream output = new ByteArrayOutputStream(prefix.length + expectedSize)) {
            output.writeBytes(prefix);
            writeTo(output, instance);
            return output.toByteArray();
        } catch (IOException impossible) {
            return rethrow(impossible);
        }
    }

    default @NotNull ByteBuffer writeToByteBuffer(@NotNull T instance) {
        return ByteBuffer.wrap(writeToBytes(instance));
    }

    default @NotNull ByteBuf writeToByteBuf(@NotNull T instance) {
        return Unpooled.wrappedBuffer(writeToBytes(instance));
    }

    @NotNull T readFrom(@NotNull InputStream input, int available) throws IOException;

    default @NotNull T readFromBytes(byte @NotNull [] bytes) {
        try (ByteArrayInputStream input = new ByteArrayInputStream(bytes)) {
            return readFrom(input, input.available());
        } catch (IOException impossible) {
            return rethrow(impossible);
        }
    }

    default @NotNull T readFromBytes(int skipBytes, byte @NotNull [] bytes) {
        assert skipBytes <= bytes.length : "Invalid prefix: skip=%d, array-length=%d".formatted(skipBytes, bytes.length);
        try (ByteArrayInputStream input = new ByteArrayInputStream(bytes, skipBytes, bytes.length)) {
            return readFrom(input, input.available());
        } catch (IOException impossible) {
            return rethrow(impossible);
        }
    }

    default @NotNull T readFromByteBuffer(@NotNull ByteBuffer buffer) {
        return readFromByteBuf(Unpooled.wrappedBuffer(buffer));
    }

    default @NotNull T readFromByteBuf(@NotNull ByteBuf buf) {
        try (ByteBufInputStream input = new ByteBufInputStream(buf)) {
            return readFrom(input, input.available());
        } catch (IOException impossible) {
            return rethrow(impossible);
        }
    }
}
