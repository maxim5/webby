package io.webby.demo.hello;

import io.webby.testing.BaseHttpIntegrationTest;
import org.junit.jupiter.api.Test;

import static io.webby.testing.AssertResponse.assert200;
import static io.webby.testing.AssertResponse.assert404;

public class AcceptGuiceIntegrationTest extends BaseHttpIntegrationTest {
    protected final AcceptGuice handler = testSetup(AcceptGuice.class).initHandler();

    @Test
    public void settings_injected() {
        assert200(get("/guice/simple"), "UTF-8");
        assert404(post("/guice/simple"));

        assert200(get("/guice/simple/2"), "UTF-8");
        assert404(post("/guice/simple/2"));

        assert200(get("/guice/param/str/foo"), "foo:UTF-8");
        assert200(get("/guice/param/int/100"), "100:UTF-8");

        assert200(get("/guice/settings_request"), "UTF-8");
        assert200(get("/guice/request_settings"), "UTF-8");
    }

    @Test
    public void settings_and_content_injected() {
        assert200(post("/guice/request_settings_content", "{}"), "{}:UTF-8");
    }
}
