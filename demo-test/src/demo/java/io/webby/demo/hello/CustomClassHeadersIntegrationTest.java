package io.webby.demo.hello;

import io.netty.handler.codec.http.HttpResponse;
import io.webby.netty.HttpConst;
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
        assertHeaders(response, HttpConst.CACHE_CONTROL, "no-cache", HttpConst.ETAG, "foobar");
    }

    @Test
    public void get_cache() {
        HttpResponse response = get("/headers/cache");
        assert200(response, "cache");
        assertContentLength(response, 5);
        assertContentType(response, TEXT_HTML_CHARSET);
        assertHeaders(response, HttpConst.CACHE_CONTROL, "only-if-cached");
    }
}
