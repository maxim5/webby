package io.webby.db.codec;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import com.google.common.primitives.Shorts;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class Codecs {
    public static int writeByte8(int value, @NotNull OutputStream output) throws IOException {
        output.write(value);
        return 1;
    }

    public static int readByte8(@NotNull InputStream input) throws IOException {
        return input.read();
    }

    public static int writeInt16(int value, @NotNull OutputStream output) throws IOException {
        output.write(Shorts.toByteArray((short) value));
        return 2;
    }

    public static short readInt16(@NotNull InputStream input) throws IOException {
        return Shorts.fromByteArray(input.readNBytes(2));
    }

    public static int writeInt32(int value, @NotNull OutputStream output) throws IOException {
        output.write(Ints.toByteArray(value));
        return 4;
    }

    public static int readInt32(@NotNull InputStream input) throws IOException {
        return Ints.fromByteArray(input.readNBytes(4));
    }

    public static int writeLong64(long value, @NotNull OutputStream output) throws IOException {
        output.write(Longs.toByteArray(value));
        return 8;
    }

    public static long readLong64(@NotNull InputStream input) throws IOException {
        return Longs.fromByteArray(input.readNBytes(8));
    }

    public static int writeBoolean8(boolean value, @NotNull OutputStream output) throws IOException {
        return writeByte8(value ? 1 : 0, output);
    }

    public static boolean readBoolean8(@NotNull InputStream input) throws IOException {
        return input.read() > 0;
    }

    public static int writeByteArray(byte[] value, @NotNull OutputStream output) throws IOException {
        if (value != null) {
            int size = writeInt32(value.length, output);
            output.write(value);
            return size + value.length;
        }
        return writeInt32(-1, output);
    }

    public static byte[] readByteArray(@NotNull InputStream input) throws IOException {
        int length = readInt32(input);
        return length >= 0 ? input.readNBytes(length) : null;
    }

    public static int writeNullableString(@Nullable String instance, @NotNull OutputStream output) throws IOException {
        byte[] bytes = instance != null ? instance.getBytes(StandardCharsets.UTF_8) : null;
        return writeByteArray(bytes, output);
    }

    public static @Nullable String readNullableString(@NotNull InputStream input) throws IOException {
        byte[] bytes = readByteArray(input);
        return bytes != null ? new String(bytes, StandardCharsets.UTF_8) : null;
    }

    public static int writeString(@NotNull String instance, @NotNull OutputStream output) throws IOException {
        return writeByteArray(instance.getBytes(StandardCharsets.UTF_8), output);
    }

    public static @NotNull String readString(@NotNull InputStream input) throws IOException {
        byte[] bytes = readByteArray(input);
        assert bytes != null;
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
