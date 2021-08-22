package io.webby.util.netty;

import io.netty.buffer.ByteBuf;
import io.netty.util.AsciiString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;

public class EasyByteBuf {
    public static @NotNull AsciiString toAsciiString(byte @NotNull [] bytes) {
        return new AsciiString(bytes, false);
    }

    public static @Nullable ByteBuf readUntil(@NotNull ByteBuf content, byte value, int maxLength) {
        int start = content.readerIndex();
        int index = content.indexOf(start, start + Math.min(maxLength, content.readableBytes()), value);
        if (index < 0) {
            return null;
        }
        ByteBuf result = content.readBytes(index - start);
        content.readBytes(1);  // Equal to `value`
        return result;
    }

    public static int parseIntSafe(@NotNull ByteBuf content, int defaultValue) {
        try {
            if (content.hasArray()) {
                return parseInt(content.array(), content.readerIndex(), content.readableBytes(), 10);
            } else {
                return Integer.parseInt(content.toString(StandardCharsets.US_ASCII));
            }
        } catch (NumberFormatException ignore) {
            return defaultValue;
        }
    }

    public static long parseLongSafe(@NotNull ByteBuf content, long defaultValue) {
        try {
            if (content.hasArray()) {
                return parseLong(content.array(), content.readerIndex(), content.readableBytes(), 10);
            } else {
                return Long.parseLong(content.toString(StandardCharsets.US_ASCII));
            }
        } catch (NumberFormatException ignore) {
            return defaultValue;
        }
    }

    public static int parseInt(byte @NotNull [] bytes, int fromIndex, int length, int radix) {
        return Integer.parseInt(toAsciiString(bytes), fromIndex, length, radix);
    }

    public static long parseLong(byte @NotNull [] bytes, int fromIndex, int length, int radix) {
        return Long.parseLong(toAsciiString(bytes), fromIndex, length, radix);
    }

    public static void writeIntString(int value, @NotNull ByteBuf dest) {
        dest.writeCharSequence(String.valueOf(value), StandardCharsets.US_ASCII);
    }

    public static void writeLongString(long value, @NotNull ByteBuf dest) {
        dest.writeCharSequence(String.valueOf(value), StandardCharsets.US_ASCII);
    }
}
