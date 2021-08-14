package io.webby.testing;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;
import io.netty.buffer.DuplicatedByteBuf;
import io.netty.buffer.Unpooled;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;

import java.nio.charset.Charset;
import java.util.Arrays;

import static io.webby.util.EasyCast.castAny;

public class TestingBytes {
    public static final Charset CHARSET = Charset.defaultCharset();
    private static final Readability READABILITY_MODE = Readability.HUMAN_READABLE;

    private enum Readability { ORIGINAL, HUMAN_READABLE, BYTE_DETAILS }

    public static byte @NotNull [] asBytes(@NotNull String str) {
        return str.getBytes(CHARSET);
    }

    public static @NotNull ByteBuf asByteBuf(@Nullable String content) {
        return asReadable(content != null ? Unpooled.copiedBuffer(content, CHARSET) : Unpooled.EMPTY_BUFFER);
    }

    public static @Nullable ByteBuf asByteBufOrNull(@Nullable String content) {
        return content != null ? asByteBuf(content) : null;
    }

    public static @Nullable String asStringOrNull(@Nullable ByteBuf buf) {
        return buf != null ? buf.toString(CHARSET) : null;
    }

    public static void assertByteBuf(@Nullable ByteBuf buf, @Nullable String expected) {
        Assertions.assertEquals(expected, TestingBytes.asStringOrNull(buf));
    }

    public static @NotNull ByteBuf asReadable(@NotNull ByteBuf buf) {
        return switch (READABILITY_MODE) {
            case ORIGINAL -> throw new IllegalStateException("Readability mode is off: %s".formatted(READABILITY_MODE));
            case HUMAN_READABLE -> new HumanReadableByteBuf(buf);
            case BYTE_DETAILS -> new ByteDetailsReadableByteBuf(buf);
        };
    }

    public static <T> @NotNull T replaceWithReadable(@NotNull T object) {
        if (READABILITY_MODE != Readability.ORIGINAL && object instanceof ByteBufHolder holder) {
            ByteBufHolder replace = holder.replace(asReadable(holder.content()));
            return castAny(replace);
        }
        return object;
    }

    @SuppressWarnings("deprecation")
    protected static class HumanReadableByteBuf extends DuplicatedByteBuf {
        public HumanReadableByteBuf(ByteBuf buffer) {
            super(buffer);
        }

        @Override
        public String toString() {
            return unwrap().toString(CHARSET);
        }
    }

    @SuppressWarnings("deprecation")
    protected static class ByteDetailsReadableByteBuf extends DuplicatedByteBuf {
        public ByteDetailsReadableByteBuf(ByteBuf buffer) {
            super(buffer);
        }

        @Override
        public String toString() {
            byte[] array = unwrap().array();
            return Arrays.toString(array);
        }
    }
}
