package io.webby.hello;

import io.webby.netty.BaseIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AcceptRequestIntegrationTest extends BaseIntegrationTest {
    @BeforeEach
    void setup() {
        testStartup(AcceptRequest.class);
    }

    @Test
    public void get_simple() {
        assert200(get("/request/simple"), "Hello <b>/request/simple</b>!");
        // assert200(get("/request/simple?key=value"), "Hello <b>/request/simple?key=value</b>"); // TODO
    }

    @Test
    public void test_string_var() {
        assert200(get("/request/str/10"), "Hello str <b>10</b> from <b>/request/str/10</b>!");
        assert200(get("/request/str/foo"), "Hello str <b>foo</b> from <b>/request/str/foo</b>!");
        assert404(get("/request/str/foo/"));
    }

    @Test
    public void test_buffer_var() {
        assert200(get("/request/buffer/foo"), "Hello buffer <b>foo</b> from <i>/request/buffer/foo</i>!");
        assert400(get("/request/buffer/foo-bar-baz"));
    }

    @Test
    public void test_two_buffer_vars() {
        assert200(get("/request/buffer/foo/bar"),
                "Hello buffer <b>foo</b> and buffer <b>bar</b> from <i>/request/buffer/foo/bar</i>!");
    }
}
