package io.webby.netty.response;

import com.google.inject.Injector;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.FullHttpResponse;
import io.webby.Testing;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
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
    public void byte_buf() {
        byte[] bytes = "foo".getBytes(Charset.defaultCharset());
        assertResponse(bytes, "foo");
        assertResponse(Unpooled.wrappedBuffer(bytes), "foo");
        assertResponse(Unpooled.copiedBuffer(bytes), "foo");
    }

    @Test
    public void char_buf() {
        char[] chars = "foo".toCharArray();
        assertResponse(chars, "foo");
    }

    @Test
    public void input_stream() {
        byte[] bytes = "foo".getBytes(Charset.defaultCharset());
        assertResponse(new ByteArrayInputStream(bytes), "foo");
        assertResponse(new BufferedInputStream(new ByteArrayInputStream(bytes)), "foo");
    }

    @Test
    public void string() {
        assertResponse("foo", "foo");
        // assertResponse(new StringBuilder("foo"), "foo");     // TODO: string fails
    }

    private void assertResponse(Object obj, String expected) {
        // System.out.println(getAllSupers(klass));
        Function<Object, FullHttpResponse> lookup = mapper.lookup(obj.getClass());
        Assertions.assertNotNull(lookup);
        FullHttpResponse response = lookup.apply(obj);
        Assertions.assertEquals(response.content().toString(Charset.defaultCharset()), expected);
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
