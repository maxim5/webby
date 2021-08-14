package io.webby.netty.response;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.HttpResponse;
import io.routekit.util.CharArray;
import io.webby.testing.Testing;
import io.webby.testing.TestingBytes;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import static io.webby.testing.AssertResponse.streamContent;

public class ResponseMapperTest {
    private final ResponseMapper mapper = Testing.testStartupNoHandlers().getInstance(ResponseMapper.class);

    @Test
    public void lookup_or_map_byte_buffer() {
        byte[] bytes = "foo".getBytes(TestingBytes.CHARSET);
        assertLookupClass(bytes, "foo");

        ByteBuf byteBuf = Unpooled.wrappedBuffer(bytes);
        assertLookupClass(byteBuf, "foo");

        ByteBuf copiedBuf = Unpooled.copiedBuffer(bytes);
        assertLookupClass(copiedBuf, "foo");

        ByteBuffer byteBuffer = byteBuf.nioBuffer();
        assertLookupClass(byteBuffer, "foo");

        ByteBuffer byteBufferWithoutArray = byteBuffer.asReadOnlyBuffer();
        Assertions.assertFalse(byteBufferWithoutArray.hasArray());
        assertLookupClass(byteBufferWithoutArray, "foo");
    }

    @Test
    public void lookup_or_map_char_buffer() {
        char[] chars = "foo".toCharArray();
        assertLookupClass(chars, "foo");

        CharBuffer charBuffer = CharBuffer.wrap(chars);
        assertLookupClass(charBuffer, "foo");

        CharBuffer charBufferWithoutArray = CharBuffer.wrap("foo");
        Assertions.assertFalse(charBufferWithoutArray.hasArray());
        assertLookupClass(charBufferWithoutArray, "foo");
    }

    @Test
    public void lookup_or_map_input_stream() {
        byte[] bytes = "foo".getBytes(TestingBytes.CHARSET);

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        assertLookupClass(byteArrayInputStream, "foo");

        BufferedInputStream bufferedInputStream = new BufferedInputStream(new ByteArrayInputStream(bytes));
        assertLookupClass(bufferedInputStream, "foo");
    }

    @Test
    public void lookup_or_map_string_like() {
        assertLookupClass("foo", "foo");

        StringBuilder stringBuilder = new StringBuilder("foo");
        assertMapInstance(stringBuilder, "foo");

        CharArray buffer = new CharArray("foo");
        assertMapInstance(buffer, "foo");
    }

    @Test
    public void lookup_or_map_other_objects_not_found() {
        assertLookupClass(new Object(), null);
        assertMapInstance(new Object(), null);

        assertLookupClass(new Object[0], null);
        assertMapInstance(new Object[0], null);
    }

    @Test
    @Disabled("Stream close is not handled by ResponseMapper anymore")
    public void should_close_input_stream() {
        byte[] bytes = "foo".getBytes(TestingBytes.CHARSET);
        AtomicBoolean closed = new AtomicBoolean(false);
        InputStream stream = new ByteArrayInputStream(bytes) {
            @Override
            public void close() {
                closed.set(true);
            }
        };
        mapper.mapInstance(stream).apply(stream);
        Assertions.assertTrue(closed.get());
    }

    @Test
    public void should_close_reader() {
        AtomicBoolean closed = new AtomicBoolean(false);
        Reader reader = new StringReader("foo") {
            @Override
            public void close() {
                super.close();
                closed.set(true);
            }
        };
        mapper.mapInstance(reader).apply(reader);
        Assertions.assertTrue(closed.get());
    }

    private void assertLookupClass(Object obj, String expected) {
        Function<Object, HttpResponse> lookup = mapper.lookupClass(obj.getClass());
        assertResponseFunction(lookup, obj, expected);
    }

    private void assertMapInstance(Object obj, String expected) {
        Function<Object, HttpResponse> lookup = mapper.mapInstance(obj);
        assertResponseFunction(lookup, obj, expected);
    }

    private void assertResponseFunction(Function<Object, HttpResponse> lookup, Object obj, String expected) {
        if (expected != null) {
            Assertions.assertNotNull(lookup, () -> describe(obj));
            HttpResponse response = lookup.apply(obj);
            TestingBytes.assertByteBuf(streamContent(response), expected);
        } else {
            Assertions.assertNull(lookup, () -> describe(obj));
        }
    }

    private static String describe(Object obj) {
        return "%s super classes: %s".formatted(obj, getAllSupers(obj.getClass()));
    }

    private static ArrayList<Class<?>> getAllSupers(Class<?> klass) {
        ArrayList<Class<?>> result = new ArrayList<>();
        while (klass != null) {
            result.add(klass);
            klass = klass.getSuperclass();
        }
        return result;
    }
}
