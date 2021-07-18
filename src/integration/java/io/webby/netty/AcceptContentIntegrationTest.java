package io.webby.netty;

import com.google.inject.Injector;
import io.webby.Testing;
import io.webby.hello.AcceptContent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AcceptContentIntegrationTest extends BaseIntegrationTest {
    @BeforeEach
    void setup() {
        Injector injector = Testing.testStartup(AcceptContent.class);
        handler = injector.getInstance(NettyChannelHandler.class);
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
