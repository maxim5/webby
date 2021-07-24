package io.webby.hello;

import io.webby.netty.BaseIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ReturnObjectIntegrationTest extends BaseIntegrationTest {
    @BeforeEach
    void setup() {
        testStartup(ReturnObject.class);
    }

    @Test
    public void test_json() {
        assert200(get("/json/bar"), """
            {"foo":1,"var":["bar"]}
        """.trim());

        assert200(get("/json/bar/baz"), """
            {"foo":1,"var":["bar","baz"]}
        """.trim());
    }
}
