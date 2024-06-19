package io.spbx.webby.demo.hello;

import io.spbx.webby.testing.BaseHttpIntegrationTest;
import org.junit.jupiter.api.Test;

import static io.spbx.webby.testing.AssertResponse.assertThat;

public class ThrowServeErrorIntegrationTest extends BaseHttpIntegrationTest {
    protected final ThrowServeError handler = testSetup(ThrowServeError.class).initHandler();

    @Test
    public void redirects() {
        assertThat(get("/r/error/307")).isTempRedirect("/");
        assertThat(get("/r/error/308")).isPermRedirect("/");
    }

    @Test
    public void serve_errors() {
        assertThat(get("/r/error/400")).is400();
        assertThat(get("/r/error/401")).is401();
        assertThat(get("/r/error/403")).is403();
        assertThat(get("/r/error/404")).is404();
        assertThat(get("/r/error/503")).is503();

        assertThat(get("/r/error/custom/200")).is200();
        assertThat(get("/r/error/custom/400")).is400();
        assertThat(get("/r/error/custom/404")).is404();
        assertThat(get("/r/error/custom/500")).is500();
    }
}
