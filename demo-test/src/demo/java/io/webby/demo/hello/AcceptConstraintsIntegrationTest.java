package io.webby.demo.hello;

import io.spbx.webby.testing.BaseHttpIntegrationTest;
import org.junit.jupiter.api.Test;

import static io.spbx.webby.testing.AssertResponse.assertThat;

public class AcceptConstraintsIntegrationTest extends BaseHttpIntegrationTest {
    protected final AcceptConstraints handler = testSetup(AcceptConstraints.class).initHandler();

    @Test
    public void get_one_string() {
        assertThat(get("/constraints/one_string/1")).is200().hasContent("1");
        assertThat(get("/constraints/one_string/12")).is200().hasContent("12");
        assertThat(get("/constraints/one_string/123")).is200().hasContent("123");
        assertThat(get("/constraints/one_string/" + "x".repeat(256))).is200();
        assertThat(get("/constraints/one_string/" + "x".repeat(257))).is400();
    }

    @Test
    public void get_one_array() {
        assertThat(get("/constraints/one_array/1")).is200().hasContent("1");
        assertThat(get("/constraints/one_array/12")).is200().hasContent("12");
        assertThat(get("/constraints/one_array/1234567890")).is200().hasContent("1234567890");
        assertThat(get("/constraints/one_array/12345678901")).is400();
    }

    @Test
    public void get_one_int() {
        assertThat(get("/constraints/one_int/2")).is200().hasContent("2");
        assertThat(get("/constraints/one_int/99")).is200().hasContent("99");
        assertThat(get("/constraints/one_int/0")).is400();
        assertThat(get("/constraints/one_int/1")).is400();
        assertThat(get("/constraints/one_int/100")).is400();
        assertThat(get("/constraints/one_int/123456789012345678901234567890")).is400();
    }

    @Test
    public void get_convert_one_string() {
        assertThat(get("/constraints/convert/one_string/1")).is200().hasContent("1");
        assertThat(get("/constraints/convert/one_string/Foo")).is200().hasContent("foo");
    }
}
