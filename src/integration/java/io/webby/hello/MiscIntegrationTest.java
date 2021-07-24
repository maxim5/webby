package io.webby.hello;

import io.webby.netty.BaseIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MiscIntegrationTest extends BaseIntegrationTest {
    @BeforeEach
    void setup() {
        testStartup(Misc.class);
    }

    @Test
    public void get_misc() {
        assert200(get("/misc/void"), "");
        assert200(get("/misc/null"), "");
        assert503(get("/error/npe"));
    }
}
