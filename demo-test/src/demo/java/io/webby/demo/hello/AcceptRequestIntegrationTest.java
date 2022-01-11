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
    public void get_one_string() {
        assert200(get("/request/one_string/10"), "Hello str <b>10</b> from <b>/request/one_string/10</b>!");
        assert200(get("/request/one_string/foo"), "Hello str <b>foo</b> from <b>/request/one_string/foo</b>!");
        assert404(get("/request/one_string/foo/"));
    }

    @Test
    public void get_one_array() {
        assert200(get("/request/one_array/foo"), "Hello buffer <b>foo</b> from <i>/request/one_array/foo</i>!");
    }

    @Test
    public void get_two_arrays() {
        assert200(get("/request/two_arrays/foo/bar"),
                  "Hello buffer <b>foo</b> and buffer <b>bar</b> from <i>/request/two_arrays/foo/bar</i>!");
    }

    @Test
    public void get_one_int() {
        assert200(get("/request/one_int/10"), "Hello 10 from <b>/request/one_int/10</b>!");
        assert200(get("/request/one_int/0"), "Hello 0 from <b>/request/one_int/0</b>!");
        assert200(get("/request/one_int/-5"), "Hello -5 from <b>/request/one_int/-5</b>!");

        assert200(get("/request/one_int/10?"), "Hello 10 from <b>/request/one_int/10</b>!");
        assert200(get("/request/one_int/10?key=value"), "Hello 10 from <b>/request/one_int/10</b>!");
        assert400(get("/request/one_int/foo"));
        assert404(get("/request/one_int/foo/"));

        assert400(get("/request/one_int/123456789012345678901234567890"));
    }

    @Test
    public void get_two_ints() {
        assert200(get("/request/two_ints/10/20"), "Hello 10 + 20 from <b>/request/two_ints/10/20</b>!");
        assert200(get("/request/two_ints/0/0"), "Hello 0 + 0 from <b>/request/two_ints/0/0</b>!");
        assert200(get("/request/two_ints/-1/-2"), "Hello -1 + -2 from <b>/request/two_ints/-1/-2</b>!");

        assert200(get("/request/two_ints/10/20?"), "Hello 10 + 20 from <b>/request/two_ints/10/20</b>!");
        assert200(get("/request/two_ints/10/20?key=value"), "Hello 10 + 20 from <b>/request/two_ints/10/20</b>!");

        assert400(get("/request/two_ints/foo/20"));
        assert400(get("/request/two_ints/10/bar"));
        assert400(get("/request/two_ints/foo/bar"));

        assert400(get("/request/two_ints/123456789012345678901234567890/10"));
        assert400(get("/request/two_ints/10/123456789012345678901234567890"));
    }
}
