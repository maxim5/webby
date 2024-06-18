package io.spbx.webby.demo.hello;

import io.netty.handler.codec.http.HttpResponse;
import io.spbx.webby.testing.BaseHttpIntegrationTest;
import org.junit.jupiter.api.Test;

import static io.spbx.webby.testing.AssertResponse.assertThat;

public class ReturnValueIntegrationTest extends BaseHttpIntegrationTest {
    protected final ReturnValue handler = testSetup(ReturnValue.class).initHandler();

    @Test
    public void byte_array_like() {
        assertThat(get("/r/bytes/foo-bar")).is200().hasContent("foo-bar");
        assertThat(get("/r/byteBuf/foo-bar")).is200().hasContent("foo-bar");
        assertThat(get("/r/byteBuffer/foo-bar")).is200().hasContent("foo-bar");
        assertThat(get("/r/stream/foo-bar")).is200().hasContent("foo-bar");
        assertThat(get("/r/byteChannel/foo-bar")).is200().hasContent("foo-bar");
    }

    @Test
    public void char_array_like() {
        assertThat(get("/r/chars/foo-bar")).is200().hasContent("foo-bar");
        assertThat(get("/r/charBuffer/foo-bar")).is200().hasContent("foo-bar");
        assertThat(get("/r/charSeq/foo-bar")).is200().hasContent("foo-bar");
        assertThat(get("/r/reader/foo-bar")).is200().hasContent("foo-bar");
    }

    @Test
    public void file_like() {
        HttpResponse response = get("/r/file/README.md");
        assertThat(response).is200().hasContentWhichContains("<h4 align=\"center\">Web Server for Humans</h4>");
    }
}
