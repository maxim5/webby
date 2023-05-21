package io.webby.demo.hello;

import io.webby.testing.BaseHttpIntegrationTest;
import org.junit.jupiter.api.Test;

import static io.webby.testing.AssertResponse.assertThat;

public class AcceptGuiceIntegrationTest extends BaseHttpIntegrationTest {
    protected final AcceptGuice handler = testSetup(AcceptGuice.class).initHandler();

    @Test
    public void settings_injected() {
        assertThat(get("/guice/simple")).is200().hasContent("UTF-8");
        assertThat(post("/guice/simple")).is404();

        assertThat(get("/guice/simple/2")).is200().hasContent("UTF-8");
        assertThat(post("/guice/simple/2")).is404();

        assertThat(get("/guice/param/str/foo")).is200().hasContent("foo:UTF-8");
        assertThat(get("/guice/param/int/100")).is200().hasContent("100:UTF-8");

        assertThat(get("/guice/settings_request")).is200().hasContent("UTF-8");
        assertThat(get("/guice/request_settings")).is200().hasContent("UTF-8");
    }

    @Test
    public void settings_and_content_injected() {
        assertThat(post("/guice/request_settings_content", "{}")).is200().hasContent("{}:UTF-8");
    }
}
