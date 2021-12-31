package io.webby.demo.hello;

import io.webby.testing.BaseHttpIntegrationTest;
import org.junit.jupiter.api.Test;

import static io.webby.testing.AssertResponse.*;

public class ThrowServeErrorIntegrationTest extends BaseHttpIntegrationTest {
    protected final ThrowServeError handler = testSetup(ThrowServeError.class).initHandler();

    @Test
    public void redirects() {
        assertTempRedirect(get("/r/error/307"), "/");
        assertPermRedirect(get("/r/error/308"), "/");
    }

    @Test
    public void serve_errors() {
        assert400(get("/r/error/400"));
        assert401(get("/r/error/401"));
        assert403(get("/r/error/403"));
        assert404(get("/r/error/404"));
        assert503(get("/r/error/503"));

        assert200(get("/r/error/custom/200"));
        assert400(get("/r/error/custom/400"));
        assert404(get("/r/error/custom/404"));
        assert500(get("/r/error/custom/500"));
    }
}
