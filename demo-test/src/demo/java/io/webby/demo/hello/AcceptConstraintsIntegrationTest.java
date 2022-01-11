package io.webby.demo.hello;

import io.webby.testing.BaseHttpIntegrationTest;
import org.junit.jupiter.api.Test;

import static io.webby.testing.AssertResponse.*;

public class AcceptConstraintsIntegrationTest extends BaseHttpIntegrationTest {
    protected final AcceptConstraints handler = testSetup(AcceptConstraints.class).initHandler();

    @Test
    public void get_one_string() {
        assert200(get("/constraints/one_string/1"), "1");
        assert200(get("/constraints/one_string/12"), "12");
        assert200(get("/constraints/one_string/123"), "123");
        assert200(get("/constraints/one_string/" + "x".repeat(256)));
        assert400(get("/constraints/one_string/" + "x".repeat(257)));
    }

    @Test
    public void get_one_array() {
        assert200(get("/constraints/one_array/1"), "1");
        assert200(get("/constraints/one_array/12"), "12");
        assert200(get("/constraints/one_array/1234567890"), "1234567890");
        assert400(get("/constraints/one_array/12345678901"));
    }

    @Test
    public void get_one_int() {
        assert200(get("/constraints/one_int/2"), "2");
        assert200(get("/constraints/one_int/99"), "99");
        assert400(get("/constraints/one_int/0"));
        assert400(get("/constraints/one_int/1"));
        assert400(get("/constraints/one_int/100"));
        assert400(get("/constraints/one_int/123456789012345678901234567890"));
    }

    @Test
    public void get_convert_one_string() {
        assert200(get("/constraints/convert/one_string/1"), "1");
        assert200(get("/constraints/convert/one_string/Foo"), "foo");
    }
}
