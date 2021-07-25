package io.webby.netty.response;

import com.google.inject.Injector;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.FullHttpResponse;
import io.routekit.util.CharBuffer;
import io.webby.Testing;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.function.Function;

public class ResponseMapperTest {
    private ResponseMapper mapper;

    @BeforeEach
    void setup() {
        Injector injector = Testing.testStartupNoHandlers();
        mapper = injector.getInstance(ResponseMapper.class);
    }

    @Test
    public void lookup_or_map_byte_buffer() {
        byte[] bytes = "foo".getBytes(Charset.defaultCharset());
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

        java.nio.CharBuffer charBuffer = java.nio.CharBuffer.wrap(chars);
        assertLookupClass(charBuffer, "foo");

        java.nio.CharBuffer charBufferWithoutArray = java.nio.CharBuffer.wrap("foo");
        Assertions.assertFalse(charBufferWithoutArray.hasArray());
        assertLookupClass(charBufferWithoutArray, "foo");
    }

    @Test
    public void lookup_or_map_input_stream() {
        byte[] bytes = "foo".getBytes(Charset.defaultCharset());

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

        CharBuffer buffer = new CharBuffer("foo");
        assertMapInstance(buffer, "foo");
    }

    @Test
    public void lookup_or_map_other_objects_not_found() {
        assertLookupClass(new Object(), null);
        assertMapInstance(new Object(), null);

        assertLookupClass(new Object[0], null);
        assertMapInstance(new Object[0], null);
    }

    private void assertLookupClass(Object obj, String expected) {
        Class<?> klass = obj.getClass();
        Function<Object, FullHttpResponse> lookup = mapper.lookupClass(klass);
        if (expected != null) {
            Assertions.assertNotNull(lookup, () -> "%s super classes: %s".formatted(obj, getAllSupers(klass)));
            FullHttpResponse response = lookup.apply(obj);
            Assertions.assertEquals(expected, response.content().toString(Charset.defaultCharset()));
        } else {
            Assertions.assertNull(lookup, () -> "%s super classes: %s".formatted(obj, getAllSupers(klass)));
        }
    }

    private void assertMapInstance(Object obj, String expected) {
        Class<?> klass = obj.getClass();
        Function<Object, FullHttpResponse> lookup = mapper.mapInstance(obj);
        if (expected != null) {
            Assertions.assertNotNull(lookup, () -> "%s super classes: %s".formatted(obj, getAllSupers(klass)));
            FullHttpResponse response = lookup.apply(obj);
            Assertions.assertEquals(expected, response.content().toString(Charset.defaultCharset()));
        } else {
            Assertions.assertNull(lookup, () -> "%s super classes: %s".formatted(obj, getAllSupers(klass)));
        }
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
