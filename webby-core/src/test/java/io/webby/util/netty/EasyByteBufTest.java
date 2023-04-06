package io.webby.util.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

import static io.webby.testing.TestingBytes.*;
import static org.junit.jupiter.api.Assertions.assertEquals;


public class EasyByteBufTest {
    public static final byte DASH = (byte) '-';
    public static final byte UNDER = (byte) '_';

    @Test
    public void copyToByteArray_simple() {
        assertBytes(EasyByteBuf.copyToByteArray(asByteBuf("")), "");
        assertBytes(EasyByteBuf.copyToByteArray(asByteBuf("foo")), "foo");
        assertBytes(EasyByteBuf.copyToByteArray(asByteBuf("\0")), "\0");
    }

    @Test
    public void copyToByteArray_copy() {
        ByteBuf byteBuf = asByteBuf("foo");
        assertBytes(EasyByteBuf.copyToByteArray(byteBuf), "foo");
        assertByteBuf(byteBuf, "foo");
    }

    @Test
    public void readUntil_simple() {
        assertByteBuf(EasyByteBuf.readUntil(asByteBuf("foo-bar"), DASH, 0, 100), "foo");
        assertByteBuf(EasyByteBuf.readUntil(asByteBuf("foo--bar"), DASH, 0, 100), "foo");
        assertByteBuf(EasyByteBuf.readUntil(asByteBuf("foo-bar"), DASH, 0, 3), null);
        assertByteBuf(EasyByteBuf.readUntil(asByteBuf("foo-bar"), UNDER, 0, 100), null);

        assertByteBuf(EasyByteBuf.readUntil(asByteBuf("---"), DASH, 0, 100), "");
        assertByteBuf(EasyByteBuf.readUntil(asByteBuf("---"), DASH, 0, 1), "");
        assertByteBuf(EasyByteBuf.readUntil(asByteBuf("---"), DASH, 0, 0), null);

        assertByteBuf(EasyByteBuf.readUntil(asByteBuf("-"), DASH, 0, 100), "");
        assertByteBuf(EasyByteBuf.readUntil(asByteBuf("-"), DASH, 0, 0), null);

        assertByteBuf(EasyByteBuf.readUntil(asByteBuf(""), DASH, 0, 100), null);
        assertByteBuf(EasyByteBuf.readUntil(asByteBuf(""), DASH, 0, 1), null);
        assertByteBuf(EasyByteBuf.readUntil(asByteBuf(""), DASH, 0, 0), null);
    }

    @Test
    public void readUntil_readIndex() {
        ByteBuf content = asByteBuf("foo-bar-baz");
        assertByteBuf(EasyByteBuf.readUntil(content, DASH, 0, 100), "foo");
        assertByteBuf(content, "bar-baz");
        assertByteBuf(EasyByteBuf.readUntil(content, DASH, 0, 100), "bar");
        assertByteBuf(content, "baz");
        assertByteBuf(EasyByteBuf.readUntil(content, DASH, 0, 100), null);
    }

    @Test
    public void readUntil_readIndex_empty() {
        ByteBuf content = asByteBuf("---");
        assertByteBuf(EasyByteBuf.readUntil(content, DASH, 0, 100), "");
        assertByteBuf(content, "--");
        assertByteBuf(EasyByteBuf.readUntil(content, DASH, 0, 100), "");
        assertByteBuf(content, "-");
        assertByteBuf(EasyByteBuf.readUntil(content, DASH, 0, 100), "");
        assertByteBuf(content, "");
        assertByteBuf(EasyByteBuf.readUntil(content, DASH, 0, 100), null);
    }

    @Test
    public void parseIntSafe_simple() {
        assertEquals(1, EasyByteBuf.parseIntSafe(asByteBuf("1"), 0));
        assertEquals(9, EasyByteBuf.parseIntSafe(asByteBuf("9"), 0));
        assertEquals(123, EasyByteBuf.parseIntSafe(asByteBuf("123"), 0));
        assertEquals(12345678, EasyByteBuf.parseIntSafe(asByteBuf("12345678"), 0));
        assertEquals(0, EasyByteBuf.parseIntSafe(asByteBuf("0"), -1));

        assertEquals(-1, EasyByteBuf.parseIntSafe(asByteBuf(""), -1));
        assertEquals(-1, EasyByteBuf.parseIntSafe(asByteBuf("foo"), -1));
        assertEquals(-1, EasyByteBuf.parseIntSafe(asByteBuf("1+2"), -1));
    }

    @Test
    public void parseIntSafe_plus_or_minus() {
        assertEquals(0, EasyByteBuf.parseIntSafe(asByteBuf("+0"), -1));
        assertEquals(0, EasyByteBuf.parseIntSafe(asByteBuf("+00"), -1));
        assertEquals(0, EasyByteBuf.parseIntSafe(asByteBuf("-0"), -1));
        assertEquals(0, EasyByteBuf.parseIntSafe(asByteBuf("-00"), -1));

        assertEquals(1, EasyByteBuf.parseIntSafe(asByteBuf("+1"), 0));
        assertEquals(100, EasyByteBuf.parseIntSafe(asByteBuf("+100"), 0));
        assertEquals(-1, EasyByteBuf.parseIntSafe(asByteBuf("-1"), 0));
        assertEquals(-100, EasyByteBuf.parseIntSafe(asByteBuf("-100"), 0));
    }

    @Test
    public void parseIntSafe_edge_cases() {
        assertEquals(Integer.MAX_VALUE, EasyByteBuf.parseIntSafe(asByteBuf("2147483647"), 0));
        assertEquals(Integer.MIN_VALUE, EasyByteBuf.parseIntSafe(asByteBuf("-2147483648"), 0));

        assertEquals(Integer.MAX_VALUE - 1, EasyByteBuf.parseIntSafe(asByteBuf("2147483646"), 0));
        assertEquals(Integer.MIN_VALUE + 1, EasyByteBuf.parseIntSafe(asByteBuf("-2147483647"), 0));

        assertEquals(0, EasyByteBuf.parseIntSafe(asByteBuf("2147483648"), 0));
        assertEquals(0, EasyByteBuf.parseIntSafe(asByteBuf("-2147483649"), 0));
        assertEquals(0, EasyByteBuf.parseIntSafe(asByteBuf("9223372036854775807"), 0));
        assertEquals(0, EasyByteBuf.parseIntSafe(asByteBuf("-9223372036854775808"), 0));
    }

    @Test
    public void parseLongSafe_simple() {
        assertEquals(1, EasyByteBuf.parseLongSafe(asByteBuf("1"), 0));
        assertEquals(9, EasyByteBuf.parseLongSafe(asByteBuf("9"), 0));
        assertEquals(123, EasyByteBuf.parseLongSafe(asByteBuf("123"), 0));
        assertEquals(12345678, EasyByteBuf.parseLongSafe(asByteBuf("12345678"), 0));
        assertEquals(0, EasyByteBuf.parseLongSafe(asByteBuf("0"), -1));

        assertEquals(-1, EasyByteBuf.parseLongSafe(asByteBuf(""), -1));
        assertEquals(-1, EasyByteBuf.parseLongSafe(asByteBuf("foo"), -1));
        assertEquals(-1, EasyByteBuf.parseLongSafe(asByteBuf("1+2"), -1));
    }

    @Test
    public void parseLongSafe_plus_or_minus() {
        assertEquals(0, EasyByteBuf.parseLongSafe(asByteBuf("+0"), -1));
        assertEquals(0, EasyByteBuf.parseLongSafe(asByteBuf("+00"), -1));
        assertEquals(0, EasyByteBuf.parseLongSafe(asByteBuf("-0"), -1));
        assertEquals(0, EasyByteBuf.parseLongSafe(asByteBuf("-00"), -1));

        assertEquals(1, EasyByteBuf.parseLongSafe(asByteBuf("+1"), 0));
        assertEquals(100, EasyByteBuf.parseLongSafe(asByteBuf("+100"), 0));
        assertEquals(-1, EasyByteBuf.parseLongSafe(asByteBuf("-1"), 0));
        assertEquals(-100, EasyByteBuf.parseLongSafe(asByteBuf("-100"), 0));
    }

    @Test
    public void parseLongSafe_edge_cases() {
        assertEquals(Long.MAX_VALUE, EasyByteBuf.parseLongSafe(asByteBuf("9223372036854775807"), 0));
        assertEquals(Long.MIN_VALUE, EasyByteBuf.parseLongSafe(asByteBuf("-9223372036854775808"), 0));

        assertEquals(Long.MAX_VALUE - 1, EasyByteBuf.parseLongSafe(asByteBuf("9223372036854775806"), 0));
        assertEquals(Long.MIN_VALUE + 1, EasyByteBuf.parseLongSafe(asByteBuf("-9223372036854775807"), 0));

        assertEquals(0, EasyByteBuf.parseLongSafe(asByteBuf("9223372036854775808"), 0));
        assertEquals(0, EasyByteBuf.parseLongSafe(asByteBuf("-9223372036854775809"), 0));
    }

    @Test
    public void writeIntString_simple() {
        assertByteBuf(withNewBuffer(content -> EasyByteBuf.writeIntString(0, content)), "0");
        assertByteBuf(withNewBuffer(content -> EasyByteBuf.writeIntString(101, content)), "101");
        assertByteBuf(withNewBuffer(content -> EasyByteBuf.writeIntString(-101, content)), "-101");
    }

    @Test
    public void writeLongString_simple() {
        assertByteBuf(withNewBuffer(content -> EasyByteBuf.writeLongString(0, content)), "0");
        assertByteBuf(withNewBuffer(content -> EasyByteBuf.writeLongString(101, content)), "101");
        assertByteBuf(withNewBuffer(content -> EasyByteBuf.writeLongString(-101, content)), "-101");
    }

    private static @NotNull ByteBuf withNewBuffer(@NotNull Consumer<ByteBuf> consumer) {
        ByteBuf buffer = Unpooled.buffer(8);
        consumer.accept(buffer);
        return buffer;
    }
}
