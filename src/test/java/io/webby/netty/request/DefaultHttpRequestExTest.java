package io.webby.netty.request;

import io.webby.testing.Testing;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.webby.testing.FakeRequests.getEx;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DefaultHttpRequestExTest {
    @BeforeEach
    void setUp() {
        Testing.testStartupNoHandlers();
    }

    @Test
    public void query_params() {
        HttpRequestEx requestEx = getEx("/?foo=bar");
        assertEquals("/", requestEx.path());
        assertEquals("foo=bar", requestEx.query());
        assertEquals(Map.of("foo", List.of("bar")), requestEx.params().getMap());
    }
}
