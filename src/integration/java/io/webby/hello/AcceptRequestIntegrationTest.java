package io.webby.hello;

import io.webby.netty.BaseIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AcceptRequestIntegrationTest extends BaseIntegrationTest {
    @BeforeEach
    void setup() {
        testStartup(AcceptRequest.class);
    }

    @Test
    public void get_simple() {
        assert200(get("/str/10"), "Hello str <b>10</b> from <b>/str/10</b>!");
        assert200(get("/str/foo"), "Hello str <b>foo</b> from <b>/str/foo</b>!");
        assert404(get("/str/foo/"));

        assert200(get("/buf/foo"), "Hello buf <b>foo</b> from <i>/buf/foo</i>!");
        assert400(get("/buf/foo-bar-baz"));
    }
}
