package io.webby.demo.hello;

import io.webby.testing.BaseHttpIntegrationTest;
import org.junit.jupiter.api.Test;

import static io.webby.testing.AssertResponse.*;

public class AcceptMultiHandlerIntegrationTest extends BaseHttpIntegrationTest {
    protected final AcceptMultiHandler handler = testSetup(AcceptMultiHandler.class).initHandler();

    @Test
    public void valid_requests() {
        assert200(get("/multi/10"), "get(10)");
        assert200(post("/multi/10", "''"), "post(10:)");
        assert200(put("/multi/10"), "put(10)");
        assert200(delete("/multi/10"), "delete(10)");
        assert200(head("/multi/10"), "head_options(10)");
        assert200(options("/multi/10"), "head_options(10)");
    }

    @Test
    public void post_content() {
        assert200(post("/multi/1", "123"), "post(1:123.0)");
        assert200(post("/multi/1", "{}"), "post(1:{})");
        assert200(post("/multi/0", "{a: 1}"), "post(0:{a=1.0})");
        assert200(post("/multi/777", "[{'x': true}]"), "post(777:[{x=true}])");

        assert400(post("/multi/1"));
        assert400(post("/multi/1", ""));
        assert400(post("/multi/1", "foo bar"));
    }

    @Test
    public void not_found() {
        assert404(get("/multi/"));
        assert404(patch("/multi/10"));
    }
}
