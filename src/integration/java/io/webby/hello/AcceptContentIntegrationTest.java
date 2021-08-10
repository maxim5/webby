package io.webby.hello;

import io.webby.testing.BaseHttpIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.webby.testing.AssertResponse.*;

public class AcceptContentIntegrationTest extends BaseHttpIntegrationTest {
    @BeforeEach
    void setup() {
        testStartup(AcceptContent.class);
    }

    @Test
    public void post_no_content() {
        assert404(post("/"));

        assert200(post("/int/10"), "{}");
        assert400(post("/int/0"));
        assert400(post("/int/-1"));
        assert400(post("/int/foo"));
    }

    @Test
    public void post_content() {
        assert200(post("/intstr/foo/10", new int[] {1, 2, 3}), "Vars: str=foo y=10 content=<[1.0, 2.0, 3.0]>");
    }
}
