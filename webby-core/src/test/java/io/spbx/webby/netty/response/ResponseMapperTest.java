package io.spbx.webby.netty.response;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.HttpResponse;
import io.spbx.util.base.CharArray;
import io.spbx.webby.netty.response.ResponseMapper;
import io.spbx.webby.testing.Testing;
import io.spbx.util.testing.TestingBytes;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import static com.google.common.truth.Truth.assertThat;
import static io.spbx.webby.testing.AssertResponse.streamContentOf;
import static io.spbx.util.testing.TestingBytes.assertBytes;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ResponseMapperTest {
    private final ResponseMapper mapper = Testing.testStartup().getInstance(ResponseMapper.class);

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
        assertThat(byteBufferWithoutArray.hasArray()).isFalse();
        assertLookupClass(byteBufferWithoutArray, "foo");
    }

    @Test
    public void lookup_or_map_char_buffer() {
        char[] chars = "foo".toCharArray();
        assertLookupClass(chars, "foo");

        CharBuffer charBuffer = CharBuffer.wrap(chars);
        assertLookupClass(charBuffer, "foo");

        CharBuffer charBufferWithoutArray = CharBuffer.wrap("foo");
        assertThat(charBufferWithoutArray.hasArray()).isFalse();
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
        assertThat(closed.get()).isTrue();
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
        assertThat(closed.get()).isTrue();
    }

    private void assertLookupClass(Object obj, String expected) {
        Function<Object, HttpResponse> lookup = mapper.lookupClass(obj.getClass());
        assertResponseFunction(lookup, obj, expected);
    }

    private void assertMapInstance(Object obj, String expected) {
        Function<Object, HttpResponse> lookup = mapper.mapInstance(obj);
        assertResponseFunction(lookup, obj, expected);
    }

    private static void assertResponseFunction(Function<Object, HttpResponse> lookup, Object obj, String expected) {
        if (expected != null) {
            assertNotNull(lookup, () -> describe(obj));
            HttpResponse response = lookup.apply(obj);
            assertBytes(streamContentOf(response)).isEqualTo(expected);
        } else {
            assertNull(lookup, () -> describe(obj));
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
