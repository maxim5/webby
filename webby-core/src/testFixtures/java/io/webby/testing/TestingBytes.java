package io.webby.testing;

import com.google.common.truth.Truth;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;
import io.netty.buffer.DuplicatedByteBuf;
import io.netty.buffer.Unpooled;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

import static io.webby.util.base.EasyCast.castAny;

public class TestingBytes {
    public static final Charset CHARSET = Testing.Internals.charset();
    private static final Readability READABILITY_MODE = Readability.HUMAN_READABLE;

    private enum Readability { ORIGINAL, HUMAN_READABLE, BYTE_DETAILS }

    public static byte @NotNull [] asBytes(@NotNull String str) {
        return str.getBytes(CHARSET);
    }

    public static @NotNull ByteBuf asByteBuf(@Nullable String content) {
        return asReadable(content != null ? Unpooled.copiedBuffer(content, CHARSET) : Unpooled.EMPTY_BUFFER);
    }

    public static @NotNull ByteBuf asByteBuf(byte @NotNull [] bytes) {
        return Unpooled.wrappedBuffer(bytes);
    }

    public static @Nullable ByteBuf asByteBufOrNull(@Nullable String content) {
        return content != null ? asByteBuf(content) : null;
    }

    public static @Nullable ByteBuf asByteBufOrNull(byte @Nullable [] bytes) {
        return bytes != null ? asByteBuf(bytes) : null;
    }

    public static @NotNull ByteArrayInputStream asByteStream(@NotNull String str) {
        return new ByteArrayInputStream(asBytes(str));
    }

    public static @NotNull String asString(byte @NotNull [] bytes) {
        return new String(bytes, CHARSET);
    }

    public static @NotNull String asString(@Nullable ByteBuf buf) {
        return buf != null ? buf.toString(CHARSET) : "";
    }

    public static @Nullable String asStringOrNull(@Nullable ByteBuf buf) {
        return buf != null ? buf.toString(CHARSET) : null;
    }

    public static @Nullable List<String> asLines(@Nullable String str) {
        return str != null ? str.lines().toList() : null;
    }

    public static @Nullable List<String> asLines(@Nullable ByteBuf buf) {
        return asLines(asStringOrNull(buf));
    }

    public static @NotNull ByteBufSubject assertBytes(byte @Nullable [] bytes) {
        return assertBytes(asByteBufOrNull(bytes));
    }

    public static @NotNull ByteBufSubject assertBytes(@Nullable ByteBuf byteBuf) {
        return new ByteBufSubject(byteBuf);
    }

    public record ByteBufSubject(@Nullable ByteBuf byteBuf) {
        public void isNull() {
            Truth.assertThat(byteBuf).isNull();
        }

        public void isEqualTo(@Nullable ByteBuf expected) {
            Truth.assertThat(asReadableOrNull(byteBuf)).isEqualTo(asReadableOrNull(expected));
        }

        public void isEqualTo(@Nullable String expected) {
            isEqualTo(asByteBufOrNull(expected));
        }

        public void isEqualTo(byte @Nullable [] expected) {
            isEqualTo(asByteBufOrNull(expected));
        }
    }

    public static @NotNull ByteBuf asReadable(@Nullable ByteBuf buf) {
        ByteBuf byteBuf = buf == null ? Unpooled.EMPTY_BUFFER : buf;
        return switch (READABILITY_MODE) {
            case ORIGINAL -> throw new IllegalStateException("Readability mode is off: %s".formatted(READABILITY_MODE));
            case HUMAN_READABLE -> byteBuf instanceof HumanReadableByteBuf ? byteBuf : new HumanReadableByteBuf(byteBuf);
            case BYTE_DETAILS -> byteBuf instanceof ByteDetailsReadableByteBuf ? byteBuf : new ByteDetailsReadableByteBuf(byteBuf);
        };
    }

    public static @Nullable ByteBuf asReadableOrNull(@Nullable ByteBuf buf) {
        return buf == null ? null : asReadable(buf);
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
            return unwrap().toString(readerIndex(), readableBytes(), CHARSET);
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
