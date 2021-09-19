package io.webby.netty.response;

import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.webby.testing.BaseHttpIntegrationTest;
import io.webby.testing.FakeRequests;
import io.webby.testing.Testing;
import org.junit.jupiter.api.Test;

import static io.webby.testing.AssertBasics.assertOneOf;
import static io.webby.testing.AssertResponse.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class StaticServingIntegrationTest extends BaseHttpIntegrationTest {
    private final StaticServing serving = Testing.testStartup(DEFAULT_SETTINGS).getInstance(StaticServing.class);

    @Test
    public void serve_exists() throws Exception {
        FullHttpResponse response = serving.serve("favicon.ico", FakeRequests.get("/favicon.ico"));
        assert200(response);
        assertEquals("15406", response.headers().get(HttpHeaderNames.CONTENT_LENGTH));
        assertOneOf(response.headers().get(HttpHeaderNames.CONTENT_TYPE), ICON_MIME_TYPES);
        assertEquals("1e5d75d6", response.headers().get(HttpHeaderNames.ETAG));
        assertNotNull(HttpCaching.DATE_FORMAT.parse(response.headers().get(HttpHeaderNames.LAST_MODIFIED)));
    }

    @Test
    public void serve_not_exists() throws Exception {
        assert404(serving.serve("not-exists.xml", FakeRequests.get("/not-exists.xml")));
    }
}
