package io.webby.netty.request;

import io.netty.handler.codec.http.cookie.DefaultCookie;
import io.webby.testing.Testing;
import io.webby.util.Counting.IntCount;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.webby.testing.AssertJson.assertJsonEquivalent;
import static io.webby.testing.FakeRequests.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DefaultHttpRequestExTest {
    @BeforeEach
    void setUp() {
        Testing.testStartupNoHandlers();
    }

    @Test
    public void query_params_simple() {
        HttpRequestEx request = wrapAsEx(get("/?foo=bar"), Map.of(), 0);
        assertEquals("/", request.path());
        assertEquals("foo=bar", request.query());
        assertEquals(Map.of("foo", List.of("bar")), request.params().getMap());
    }

    @Test
    public void contentAsJson_simple() {
        HttpRequestEx request = wrapAsEx(post("/", "{\"val\": 123}"), Map.of(), 0);
        Map<?, ?> map = request.contentAsJson(Map.class);
        assertJsonEquivalent(map, Map.of("val", 123));
        IntCount intCount = request.contentAsJson(IntCount.class);
        assertEquals(123, intCount.val);
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
