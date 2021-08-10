package io.webby.hello;

import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.webby.netty.BaseHttpIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.webby.AssertResponse.*;

public class ReturnValueIntegrationTest extends BaseHttpIntegrationTest {
    @BeforeEach
    void setup() {
        testStartup(ReturnValue.class);
    }

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

    @Test
    public void json_map() {
        HttpResponse response = get("/r/json/map/bar");
        assert200(response, """
            {"foo":1,"var":["bar"]}
        """.trim());
        assertHeaders(response, HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON);
    }

    @Test
    public void json_map_value_with_slash() {
        HttpResponse response = get("/r/json/map/bar/baz");
        assert200(response, """
            {"foo":1,"var":["bar","baz"]}
        """.trim());
        assertHeaders(response, HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON);
    }

    @Test
    public void json_tree() {
        HttpResponse response = get("/r/json/tree/foobar");
        assert200(response, """
            ["f","o","o","b","a","r"]
        """.trim());
        assertHeaders(response, HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON);
    }
}
