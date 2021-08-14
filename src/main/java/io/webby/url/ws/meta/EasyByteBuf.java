package io.webby.url.ws.meta;

import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;

public class EasyByteBuf {
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

    public static long parseLongSafely(@NotNull ByteBuf content, long defaultValue) {
        if (content.hasArray()) {
            return parseLongSafely(content.array(), content.readerIndex(), content.readableBytes(), defaultValue);
        } else {
            String s = content.toString(StandardCharsets.US_ASCII);
            try {
                return Long.parseLong(s);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
    }

    // Does not support negative yet
    // Does not check the boundary
    public static long parseLongSafely(byte @NotNull [] bytes, int fromIndex, int length, long defaultValue) {
        assert 0 <= fromIndex : "Invalid from index: %d".formatted(fromIndex);
        assert length >= 0 : "Invalid length: %d".formatted(length);
        assert fromIndex + length <= bytes.length : "Invalid to index: %d".formatted(fromIndex + length);

        long result = 0;
        for (int i = fromIndex, endIndex = fromIndex + length; i < endIndex; i++) {
            int b = bytes[i];
            if (b < '0' || b > '9') {
                return defaultValue;
            }
            result = result * 10 + (b - '0');
        }
        return result;
    }

    public static void writeIntString(long value, @NotNull ByteBuf dest) {
        dest.writeCharSequence(String.valueOf(value), StandardCharsets.US_ASCII);
    }

    public static void writeLongString(long value, @NotNull ByteBuf dest) {
        dest.writeCharSequence(String.valueOf(value), StandardCharsets.US_ASCII);
    }
}
