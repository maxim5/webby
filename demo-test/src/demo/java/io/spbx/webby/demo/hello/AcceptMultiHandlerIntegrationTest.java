package io.spbx.webby.demo.hello;

import io.spbx.webby.testing.BaseHttpIntegrationTest;
import org.junit.jupiter.api.Test;

import static io.spbx.webby.testing.AssertResponse.assertThat;

public class AcceptMultiHandlerIntegrationTest extends BaseHttpIntegrationTest {
    protected final AcceptMultiHandler handler = testSetup(AcceptMultiHandler.class).initHandler();

    @Test
    public void valid_requests() {
        assertThat(get("/multi/10")).is200().hasContent("get(10)");
        assertThat(post("/multi/10", "''")).is200().hasContent("post(10:)");
        assertThat(put("/multi/10")).is200().hasContent("put(10)");
        assertThat(delete("/multi/10")).is200().hasContent("delete(10)");
        assertThat(head("/multi/10")).is200().hasContent("head_options(10)");
        assertThat(options("/multi/10")).is200().hasContent("head_options(10)");
    }

    @Test
    public void post_content() {
        assertThat(post("/multi/1", "123")).is200().hasContent("post(1:123.0)");
        assertThat(post("/multi/1", "{}")).is200().hasContent("post(1:{})");
        assertThat(post("/multi/0", "{a: 1}")).is200().hasContent("post(0:{a=1.0})");
        assertThat(post("/multi/777", "[{'x': true}]")).is200().hasContent("post(777:[{x=true}])");

        assertThat(post("/multi/1")).is400();
        assertThat(post("/multi/1", "")).is400();
        assertThat(post("/multi/1", "foo bar")).is400();
    }

    @Test
    public void not_found() {
        assertThat(get("/multi/")).is404();
        assertThat(patch("/multi/10")).is404();
    }
}
