package io.webby.db.codec;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.webby.common.SystemProperties;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static io.webby.util.Rethrow.rethrow;

// Aka Serializer
public interface Codec<T> {
    @CanIgnoreReturnValue
    int writeTo(@NotNull OutputStream output, @NotNull T instance) throws IOException;

    default byte @NotNull [] writeToBytes(@NotNull T instance) {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream(SystemProperties.DEFAULT_SIZE_BYTES)) {
            writeTo(output, instance);
            return output.toByteArray();
        } catch (IOException impossible) {
            return rethrow(impossible);
        }
    }

    @NotNull T readFrom(@NotNull InputStream input, int available) throws IOException;

    default @NotNull T readFrom(@NotNull ByteBuf buf) {
        try (ByteBufInputStream input = new ByteBufInputStream(buf)) {
            return readFrom(input, input.available());
        } catch (IOException impossible) {
            return rethrow(impossible);
        }
    }
}
