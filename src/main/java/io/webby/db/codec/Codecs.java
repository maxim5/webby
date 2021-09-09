package io.webby.db.codec;

import com.google.common.base.Utf8;
import com.google.common.flogger.FluentLogger;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import com.google.common.primitives.Shorts;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

public class Codecs {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    public static final int INT8_SIZE = Byte.BYTES;
    public static final int INT16_SIZE = Short.BYTES;
    public static final int INT32_SIZE = Integer.BYTES;
    public static final int INT64_SIZE = Long.BYTES;

    public static int writeByte8(int value, @NotNull OutputStream output) throws IOException {
        assert Byte.MIN_VALUE <= value && value <= Byte.MAX_VALUE : "Value does not fit in byte: %d".formatted(value);
        output.write(value);
        return INT8_SIZE;
    }

    public static int readByte8(@NotNull InputStream input) throws IOException {
        return input.read();
    }

    public static int writeInt16(int value, @NotNull OutputStream output) throws IOException {
        assert Short.MIN_VALUE <= value && value <= Short.MAX_VALUE : "Value does not fit in short: %d".formatted(value);
        output.write(Shorts.toByteArray((short) value));
        return INT16_SIZE;
    }

    public static short readInt16(@NotNull InputStream input) throws IOException {
        return Shorts.fromByteArray(input.readNBytes(INT16_SIZE));
    }

    public static int writeInt32(int value, @NotNull OutputStream output) throws IOException {
        output.write(Ints.toByteArray(value));
        return INT32_SIZE;
    }

    public static int readInt32(@NotNull InputStream input) throws IOException {
        return Ints.fromByteArray(input.readNBytes(INT32_SIZE));
    }

    public static int writeLong64(long value, @NotNull OutputStream output) throws IOException {
        output.write(Longs.toByteArray(value));
        return INT64_SIZE;
    }

    public static long readLong64(@NotNull InputStream input) throws IOException {
        return Longs.fromByteArray(input.readNBytes(INT64_SIZE));
    }

    public static int writeBoolean8(boolean value, @NotNull OutputStream output) throws IOException {
        return writeByte8(value ? 1 : 0, output);
    }

    public static boolean readBoolean8(@NotNull InputStream input) throws IOException {
        return input.read() > 0;
    }

    public static int writeByteArray(byte @NotNull [] value, @NotNull OutputStream output) throws IOException {
        int size = writeInt32(value.length, output);
        output.write(value);
        return size + value.length;
    }

    public static byte @NotNull [] readByteArray(@NotNull InputStream input) throws IOException {
        int length = readInt32(input);
        return input.readNBytes(length);
    }

    public static int writeNullableByteArray(byte @Nullable [] value, @NotNull OutputStream output) throws IOException {
        if (value != null) {
            return writeByteArray(value, output);
        }
        return writeInt32(-1, output);
    }

    public static byte @Nullable [] readNullableByteArray(@NotNull InputStream input) throws IOException {
        int length = readInt32(input);
        return length >= 0 ? input.readNBytes(length) : null;
    }

    public static int writeShortByteArray(byte @NotNull [] value, @NotNull OutputStream output) throws IOException {
        int size = writeInt16(value.length, output);
        output.write(value);
        return size + value.length;
    }

    public static byte @NotNull [] readShortByteArray(@NotNull InputStream input) throws IOException {
        int length = readInt16(input);
        return input.readNBytes(length);
    }

    public static int writeShortNullableByteArray(byte @Nullable [] value, @NotNull OutputStream output) throws IOException {
        if (value != null) {
            return writeShortByteArray(value, output);
        }
        return writeInt16(-1, output);
    }

    public static byte @Nullable [] readShortNullableByteArray(@NotNull InputStream input) throws IOException {
        int length = readInt16(input);
        return length >= 0 ? input.readNBytes(length) : null;
    }

    public static int writeString(@NotNull String value, @NotNull Charset charset, @NotNull OutputStream output) throws IOException {
        return writeByteArray(value.getBytes(charset), output);
    }

    public static @NotNull String readString(@NotNull InputStream input, @NotNull Charset charset) throws IOException {
        byte[] bytes = readByteArray(input);
        return new String(bytes, charset);
    }

    public static int writeNullableString(@Nullable String value, @NotNull Charset charset, @NotNull OutputStream output) throws IOException {
        byte[] bytes = value != null ? value.getBytes(charset) : null;
        return writeNullableByteArray(bytes, output);
    }

    public static @Nullable String readNullableString(@NotNull InputStream input, @NotNull Charset charset) throws IOException {
        byte[] bytes = readNullableByteArray(input);
        return bytes != null ? new String(bytes, charset) : null;
    }

    public static int writeShortString(@NotNull String value, @NotNull Charset charset, @NotNull OutputStream output) throws IOException {
        return writeShortByteArray(value.getBytes(charset), output);
    }

    public static @NotNull String readShortString(@NotNull InputStream input, @NotNull Charset charset) throws IOException {
        byte[] bytes = readShortByteArray(input);
        return new String(bytes, charset);
    }

    public static int writeShortNullableString(@Nullable String value, @NotNull Charset charset, @NotNull OutputStream output) throws IOException {
        byte[] bytes = value != null ? value.getBytes(charset) : null;
        return writeShortNullableByteArray(bytes, output);
    }

    public static @Nullable String readShortNullableString(@NotNull InputStream input, @NotNull Charset charset) throws IOException {
        byte[] bytes = readShortNullableByteArray(input);
        return bytes != null ? new String(bytes, charset) : null;
    }

    public static int byteArraySize(byte @NotNull [] value) {
        return INT32_SIZE + value.length;
    }

    public static int nullableByteArraySize(byte @Nullable [] value) {
        return INT32_SIZE + (value != null ? value.length : 0);
    }

    public static int shortByteArraySize(byte @NotNull [] value) {
        return INT16_SIZE + value.length;
    }

    public static int shortNullableByteArraySize(byte @Nullable [] value) {
        return INT16_SIZE + (value != null ? value.length : 0);
    }

    public static int stringSize(@NotNull String value, @NotNull Charset charset) {
        int byteLength = getByteLength(value, charset);
        return byteLength >= 0 ? INT32_SIZE + byteLength : -1;
    }

    public static int nullableStringSize(@Nullable String value, @NotNull Charset charset) {
        int byteLength = getByteLength(value, charset);
        return byteLength >= 0 ? INT32_SIZE + byteLength : -1;
    }

    public static int shortStringSize(@NotNull String value, @NotNull Charset charset) {
        int byteLength = getByteLength(value, charset);
        return byteLength >= 0 ? INT16_SIZE + byteLength : -1;
    }

    public static int nullableShortStringSize(@Nullable String value, @NotNull Charset charset) {
        int byteLength = getByteLength(value, charset);
        return byteLength >= 0 ? INT16_SIZE + byteLength : -1;
    }

    @SuppressWarnings("UnstableApiUsage")
    @VisibleForTesting
    static int getByteLength(@Nullable CharSequence s, @NotNull Charset charset) {
        if (s == null || s.isEmpty()) {
            return 0;
        }
        if (charset == StandardCharsets.UTF_8) {
            try {
                return Utf8.encodedLength(s);
            } catch (IllegalArgumentException e) {
                log.at(Level.WARNING).withCause(e).log("Failed to get bytes length by decoding UTF-8: %s", s);
            }
        }
        if (charset == StandardCharsets.UTF_16) {
            return 2 * s.length();
        }
        return -1;
    }
}
