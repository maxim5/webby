package io.webby.examples.hello;

import io.netty.handler.codec.http.HttpResponse;
import io.webby.testing.BaseHttpIntegrationTest;
import org.junit.jupiter.api.Test;

import static io.webby.testing.AssertResponse.assert200;
import static io.webby.testing.AssertResponse.assertContentContains;

public class ReturnValueIntegrationTest extends BaseHttpIntegrationTest {
    protected final ReturnValue handler = testSetup(ReturnValue.class).initHandler();

    @Test
    public void byte_array_like() {
        assert200(get("/r/bytes/foo-bar"), "foo-bar");
        assert200(get("/r/byteBuf/foo-bar"), "foo-bar");
        assert200(get("/r/byteBuffer/foo-bar"), "foo-bar");
        assert200(get("/r/stream/foo-bar"), "foo-bar");
        assert200(get("/r/byteChannel/foo-bar"), "foo-bar");
    }

    @Test
    public void char_array_like() {
        assert200(get("/r/chars/foo-bar"), "foo-bar");
        assert200(get("/r/charBuffer/foo-bar"), "foo-bar");
        assert200(get("/r/charSeq/foo-bar"), "foo-bar");
        assert200(get("/r/reader/foo-bar"), "foo-bar");
    }

    @Test
    public void file_like() {
        HttpResponse response = get("/r/file/README.md");
        assert200(response);
        assertContentContains(response, "<h4 align=\"center\">Web Server for Humans</h4>");
    }
}
