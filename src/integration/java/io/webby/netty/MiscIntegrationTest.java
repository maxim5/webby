package io.webby.netty;

import com.google.inject.Injector;
import io.webby.Testing;
import io.webby.hello.Misc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MiscIntegrationTest extends BaseIntegrationTest {
    @BeforeEach
    void setup() {
        Injector injector = Testing.testStartup(Misc.class);
        handler = injector.getInstance(NettyChannelHandler.class);
    }

    @Test
    public void get_misc() {
        assert200(get("/misc/void"), "");
        assert200(get("/misc/null"), "");
        assert503(get("/error/npe"));
    }
}
