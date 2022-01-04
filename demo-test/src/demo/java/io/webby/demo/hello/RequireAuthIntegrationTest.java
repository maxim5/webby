package io.webby.demo.hello;

import io.webby.testing.BaseHttpIntegrationTest;
import org.junit.jupiter.api.Test;

import static io.webby.testing.AssertResponse.assert200;
import static io.webby.testing.AssertResponse.assert401;

public class RequireAuthIntegrationTest extends BaseHttpIntegrationTest {
    protected final RequireAuth handler = testSetup(RequireAuth.class).initHandler();

    @Test
    public void access() {
        assert401(get("/access/auth_only"));
        assert401(get("/access/admin_only"));
        assert200(get("/access/public"));
    }
}
