package io.webby.demo.hello;

import io.netty.handler.codec.http.HttpResponse;
import io.webby.netty.HttpConst;
import io.webby.testing.BaseHttpIntegrationTest;
import org.junit.jupiter.api.Test;

import static io.webby.testing.AssertResponse.TEXT_HTML_CHARSET;
import static io.webby.testing.AssertResponse.assertThat;

public class CustomClassHeadersIntegrationTest extends BaseHttpIntegrationTest {
    protected final CustomClassHeaders handler = testSetup(CustomClassHeaders.class).initHandler();

    @Test
    public void get_etag() {
        HttpResponse response = get("/headers/etag");
        assertThat(response)
            .is200()
            .hasContent("etag")
            .hasContentLength(4)
            .hasContentType(TEXT_HTML_CHARSET)
            .hasHeader(HttpConst.CACHE_CONTROL, "no-cache")
            .hasHeader(HttpConst.ETAG, "foobar");
    }

    @Test
    public void get_cache() {
        HttpResponse response = get("/headers/cache");
        assertThat(response)
            .is200()
            .hasContent("cache")
            .hasContentLength(5)
            .hasContentType(TEXT_HTML_CHARSET)
            .hasHeader(HttpConst.CACHE_CONTROL, "only-if-cached");
    }
}
