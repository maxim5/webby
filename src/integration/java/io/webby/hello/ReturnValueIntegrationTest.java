package io.webby.hello;

import io.webby.netty.BaseIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ReturnValueIntegrationTest extends BaseIntegrationTest {
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
        // assert200(get("/r/byteChannel/foo-bar"), "foo-bar");     // TODO: not supported yet
    }

    @Test
    public void char_array_like() {
        assert200(get("/r/chars/foo-bar"), "foo-bar");
        assert200(get("/r/charSeq/foo-bar"), "foo-bar");
    }

    @Test
    public void json_map() {
        assert200(get("/r/json/bar"), """
            {"foo":1,"var":["bar"]}
        """.trim());

        assert200(get("/r/json/bar/baz"), """
            {"foo":1,"var":["bar","baz"]}
        """.trim());
    }
}
