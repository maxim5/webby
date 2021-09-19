package io.webby.examples.hello;

import io.netty.handler.codec.http.HttpResponse;
import io.webby.netty.response.ResponseHeaders;
import io.webby.testing.BaseHttpIntegrationTest;
import org.junit.jupiter.api.Test;

import static io.webby.testing.AssertResponse.*;

public class CustomClassHeadersIntegrationTest extends BaseHttpIntegrationTest {
    protected final CustomClassHeaders handler = testSetup(CustomClassHeaders.class).initHandler();

    @Test
    public void get_etag() {
        HttpResponse response = get("/headers/etag");
        assert200(response, "etag");
        assertContentLength(response, 4);
        assertContentType(response, TEXT_HTML_CHARSET);
        assertHeaders(response, ResponseHeaders.CACHE_CONTROL, "no-cache", ResponseHeaders.ETAG, "foobar");
    }

    @Test
    public void get_cache() {
        HttpResponse response = get("/headers/cache");
        assert200(response, "cache");
        assertContentLength(response, 5);
        assertContentType(response, TEXT_HTML_CHARSET);
        assertHeaders(response, ResponseHeaders.CACHE_CONTROL, "only-if-cached");
    }
}
