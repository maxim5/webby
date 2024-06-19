package io.spbx.webby.demo.hello;

import io.spbx.webby.testing.BaseHttpIntegrationTest;
import org.junit.jupiter.api.Test;

import static io.spbx.webby.testing.AssertResponse.assertThat;

public class AcceptRequestIntegrationTest extends BaseHttpIntegrationTest {
    protected final AcceptRequest handler = testSetup(AcceptRequest.class).initHandler();

    @Test
    public void get_simple() {
        assertThat(get("/request/simple")).is200().hasContent("Hi <b>/request/simple</b>!");
        assertThat(get("/request/simple?")).is200().hasContent("Hi <b>/request/simple?</b>!");
        assertThat(get("/request/simple?key=value")).is200().hasContent("Hi <b>/request/simple?key=value</b>!");
        assertThat(get("/request/simple#")).is200().hasContent("Hi <b>/request/simple#</b>!");
    }

    @Test
    public void get_one_string() {
        assertThat(get("/request/one_string/10")).is200().hasContent("Hi str <b>10</b> from <b>/request/one_string/10</b>!");
        assertThat(get("/request/one_string/foo")).is200().hasContent("Hi str <b>foo</b> from <b>/request/one_string/foo</b>!");
        assertThat(get("/request/one_string/foo/")).is404();
    }

    @Test
    public void get_one_array() {
        assertThat(get("/request/one_array/foo")).is200()
            .hasContent("Hi buffer <b>foo</b> from <i>/request/one_array/foo</i>!");
    }

    @Test
    public void get_two_arrays() {
        assertThat(get("/request/two_arrays/foo/bar")).is200()
            .hasContent("Hi buffer <b>foo</b> and buffer <b>bar</b> from <i>/request/two_arrays/foo/bar</i>!");
    }

    @Test
    public void get_one_int() {
        assertThat(get("/request/one_int/10")).is200().hasContent("Hi 10 from <b>/request/one_int/10</b>!");
        assertThat(get("/request/one_int/0")).is200().hasContent("Hi 0 from <b>/request/one_int/0</b>!");
        assertThat(get("/request/one_int/-5")).is200().hasContent("Hi -5 from <b>/request/one_int/-5</b>!");

        assertThat(get("/request/one_int/10?")).is200().hasContent("Hi 10 from <b>/request/one_int/10</b>!");
        assertThat(get("/request/one_int/10?key=value")).is200().hasContent("Hi 10 from <b>/request/one_int/10</b>!");
        assertThat(get("/request/one_int/foo")).is400();
        assertThat(get("/request/one_int/foo/")).is404();

        assertThat(get("/request/one_int/123456789012345678901234567890")).is400();
    }

    @Test
    public void get_two_ints() {
        assertThat(get("/request/two_ints/10/20")).is200().hasContent("Hi 10 + 20 from <b>/request/two_ints/10/20</b>!");
        assertThat(get("/request/two_ints/0/0")).is200().hasContent("Hi 0 + 0 from <b>/request/two_ints/0/0</b>!");
        assertThat(get("/request/two_ints/-1/-2")).is200().hasContent("Hi -1 + -2 from <b>/request/two_ints/-1/-2</b>!");

        assertThat(get("/request/two_ints/10/20?")).is200().hasContent("Hi 10 + 20 from <b>/request/two_ints/10/20</b>!");
        assertThat(get("/request/two_ints/10/20?key=val")).is200().hasContent("Hi 10 + 20 from <b>/request/two_ints/10/20</b>!");

        assertThat(get("/request/two_ints/foo/20")).is400();
        assertThat(get("/request/two_ints/10/bar")).is400();
        assertThat(get("/request/two_ints/foo/bar")).is400();

        assertThat(get("/request/two_ints/123456789012345678901234567890/10")).is400();
        assertThat(get("/request/two_ints/10/123456789012345678901234567890")).is400();
    }
}
