package io.webby.ws.meta;

import io.netty.buffer.ByteBuf;
import org.junit.jupiter.api.Test;

import static io.webby.testing.TestingBytes.asByteBuf;
import static io.webby.testing.TestingBytes.assertByteBuf;
import static org.junit.jupiter.api.Assertions.assertEquals;


public class EasyByteBufTest {
    public static final byte DASH = (byte) '-';
    public static final byte UNDER = (byte) '_';

    @Test
    public void readUntil_simple() {
        assertByteBuf(EasyByteBuf.readUntil(asByteBuf("foo-bar"), DASH, 100), "foo");
        assertByteBuf(EasyByteBuf.readUntil(asByteBuf("foo--bar"), DASH, 100), "foo");
        assertByteBuf(EasyByteBuf.readUntil(asByteBuf("foo-bar"), DASH, 3), null);
        assertByteBuf(EasyByteBuf.readUntil(asByteBuf("foo-bar"), UNDER, 100), null);

        assertByteBuf(EasyByteBuf.readUntil(asByteBuf("---"), DASH, 100), "");
        assertByteBuf(EasyByteBuf.readUntil(asByteBuf("---"), DASH, 1), "");
        assertByteBuf(EasyByteBuf.readUntil(asByteBuf("---"), DASH, 0), null);

        assertByteBuf(EasyByteBuf.readUntil(asByteBuf("-"), DASH, 100), "");
        assertByteBuf(EasyByteBuf.readUntil(asByteBuf("-"), DASH, 0), null);

        assertByteBuf(EasyByteBuf.readUntil(asByteBuf(""), DASH, 100), null);
        assertByteBuf(EasyByteBuf.readUntil(asByteBuf(""), DASH, 1), null);
        assertByteBuf(EasyByteBuf.readUntil(asByteBuf(""), DASH, 0), null);
    }

    @Test
    public void readUntil_readIndex() {
        ByteBuf content = asByteBuf("foo-bar-baz");
        assertByteBuf(EasyByteBuf.readUntil(content, DASH, 100), "foo");
        assertByteBuf(content, "bar-baz");
        assertByteBuf(EasyByteBuf.readUntil(content, DASH, 100), "bar");
        assertByteBuf(content, "baz");
        assertByteBuf(EasyByteBuf.readUntil(content, DASH, 100), null);
    }

    @Test
    public void readUntil_readIndex_empty() {
        ByteBuf content = asByteBuf("---");
        assertByteBuf(EasyByteBuf.readUntil(content, DASH, 100), "");
        assertByteBuf(content, "--");
        assertByteBuf(EasyByteBuf.readUntil(content, DASH, 100), "");
        assertByteBuf(content, "-");
        assertByteBuf(EasyByteBuf.readUntil(content, DASH, 100), "");
        assertByteBuf(content, "");
        assertByteBuf(EasyByteBuf.readUntil(content, DASH, 100), null);
    }

    @Test
    public void parseLongSafely_simple() {
        assertEquals(1, EasyByteBuf.parseLongSafely(asByteBuf("1"), 0));
        assertEquals(9, EasyByteBuf.parseLongSafely(asByteBuf("9"), 0));
        assertEquals(123, EasyByteBuf.parseLongSafely(asByteBuf("123"), 0));
        assertEquals(12345678, EasyByteBuf.parseLongSafely(asByteBuf("12345678"), 0));
        assertEquals(0, EasyByteBuf.parseLongSafely(asByteBuf("0"), -1));
        assertEquals(0, EasyByteBuf.parseLongSafely(asByteBuf(""), -1));
        assertEquals(Long.MAX_VALUE, EasyByteBuf.parseLongSafely(asByteBuf(String.valueOf(Long.MAX_VALUE)), 0));

        assertEquals(-1, EasyByteBuf.parseLongSafely(asByteBuf("foo"), -1));
        assertEquals(-1, EasyByteBuf.parseLongSafely(asByteBuf("1+2"), -1));
    }
}
