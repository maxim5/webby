package io.webby.netty.request;

import io.netty.handler.codec.http.cookie.DefaultCookie;
import io.webby.testing.Testing;
import io.webby.util.base.EasyPrimitives.MutableInt;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.webby.testing.AssertJson.assertJsonEquivalent;
import static io.webby.testing.FakeRequests.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DefaultHttpRequestExTest {
    @BeforeEach
    void setUp() {
        Testing.testStartup();
    }

    @Test
    public void query_params_simple() {
        HttpRequestEx request = wrapAsEx(get("/?foo=bar"), Map.of(), 0);
        assertEquals("/", request.path());
        assertEquals("foo=bar", request.query());
        assertEquals(Map.of("foo", List.of("bar")), request.params().getMap());
    }

    @Test
    public void contentAs_empty() {
        HttpRequestEx request = wrapAsEx(post("/"), Map.of(), 0);
        assertEquals("", request.contentAsString());
        assertThrows(IllegalArgumentException.class, () -> request.contentAsJson(Map.class));
        assertThrows(IllegalArgumentException.class, () -> request.contentAsJson(List.class));
        assertThrows(IllegalArgumentException.class, () -> request.contentAsJson(Object.class));
    }

    @Test
    public void contentAs_simple() {
        HttpRequestEx request = wrapAsEx(post("/", "{\"value\": 123}"), Map.of(), 0);
        assertEquals("{\"value\": 123}", request.contentAsString());
        Map<?, ?> map = request.contentAsJson(Map.class);
        assertJsonEquivalent(map, Map.of("value", 123));
        MutableInt counter = request.contentAsJson(MutableInt.class);
        assertEquals(123, counter.value);
    }

    @Test
    public void cookies_null() {
        HttpRequestEx request = wrapAsEx(get("/?foo=bar"), Map.of(), 0);
        assertEquals(List.of(), request.cookies());
    }

    @Test
    public void cookies_simple() {
        HttpRequestEx request = wrapAsEx(get("/?foo=bar"), Map.of(), 0)
                .withHeaders(Map.of("Cookie", "foo=bar; __name__=123; "));
        assertEquals(List.of(new DefaultCookie("foo", "bar"), new DefaultCookie("__name__", "123")), request.cookies());
    }
}
