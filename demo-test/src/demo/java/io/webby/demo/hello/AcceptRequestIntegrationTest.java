package io.webby.demo.hello;

import io.webby.testing.BaseHttpIntegrationTest;
import org.junit.jupiter.api.Test;

import static io.webby.testing.AssertResponse.*;

public class AcceptRequestIntegrationTest extends BaseHttpIntegrationTest {
    protected final AcceptRequest handler = testSetup(AcceptRequest.class).initHandler();

    @Test
    public void get_simple() {
        assert200(get("/request/simple"), "Hello <b>/request/simple</b>!");
        assert200(get("/request/simple?"), "Hello <b>/request/simple?</b>!");
        assert200(get("/request/simple?key=value"), "Hello <b>/request/simple?key=value</b>!");
        assert200(get("/request/simple#"), "Hello <b>/request/simple#</b>!");
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

    @Test
    public void test_int_var() {
        assert200(get("/request/int/10"), "Hello 10 from <b>/request/int/10</b>!");
        assert200(get("/request/int/10?"), "Hello 10 from <b>/request/int/10</b>!");
        assert200(get("/request/int/10?key=value"), "Hello 10 from <b>/request/int/10</b>!");
        assert400(get("/request/int/foo"));
        assert404(get("/request/int/foo/"));
    }

    @Test
    public void test_two_int_vars() {
        assert200(get("/request/int/10/20"), "Hello 10 + 20 from <b>/request/int/10/20</b>!");
        assert200(get("/request/int/10/20?"), "Hello 10 + 20 from <b>/request/int/10/20</b>!");
        assert200(get("/request/int/10/20?key=value"), "Hello 10 + 20 from <b>/request/int/10/20</b>!");

        assert400(get("/request/int/foo/20"));
        assert400(get("/request/int/10/bar"));
        assert400(get("/request/int/foo/bar"));
    }
}
