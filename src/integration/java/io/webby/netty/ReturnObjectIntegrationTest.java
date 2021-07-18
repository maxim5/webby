package io.webby.netty;

import com.google.inject.Injector;
import io.webby.Testing;
import io.webby.hello.ReturnObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ReturnObjectIntegrationTest extends BaseIntegrationTest {
    @BeforeEach
    void setup() {
        Injector injector = Testing.testStartup(ReturnObject.class);
        handler = injector.getInstance(NettyChannelHandler.class);
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
