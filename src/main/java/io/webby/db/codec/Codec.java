package io.webby.db.codec;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import org.jetbrains.annotations.NotNull;

import java.io.*;

import static io.webby.util.Rethrow.rethrow;

// Aka Serializer
public interface Codec<T> {
    @NotNull CodecSize size();

    @CanIgnoreReturnValue
    int writeTo(@NotNull OutputStream output, @NotNull T instance) throws IOException;

    default byte @NotNull [] writeToBytes(@NotNull T instance) {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream((int) size().numBytes())) {
            writeTo(output, instance);
            return output.toByteArray();
        } catch (IOException impossible) {
            return rethrow(impossible);
        }
    }

    default byte @NotNull [] writeToBytes(byte @NotNull [] prefix, @NotNull T instance) {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream(prefix.length + (int) size().numBytes())) {
            output.writeBytes(prefix);
            writeTo(output, instance);
            return output.toByteArray();
        } catch (IOException impossible) {
            return rethrow(impossible);
        }
    }

    @NotNull T readFrom(@NotNull InputStream input, int available) throws IOException;

    default @NotNull T readFrom(byte @NotNull [] bytes) {
        try (ByteArrayInputStream input = new ByteArrayInputStream(bytes)) {
            return readFrom(input, input.available());
        } catch (IOException impossible) {
            return rethrow(impossible);
        }
    }

    default @NotNull T readFrom(@NotNull ByteBuf buf) {
        try (ByteBufInputStream input = new ByteBufInputStream(buf)) {
            return readFrom(input, input.available());
        } catch (IOException impossible) {
            return rethrow(impossible);
        }
    }
}
