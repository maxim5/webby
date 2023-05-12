package io.webby.demo.hello;

import io.webby.testing.BaseHttpIntegrationTest;
import org.junit.jupiter.api.Test;

import static io.webby.testing.AssertResponse.assertThat;

public class RequireAuthIntegrationTest extends BaseHttpIntegrationTest {
    protected final RequireAuth handler = testSetup(RequireAuth.class).initHandler();

    @Test
    public void access() {
        assertThat(get("/access/auth_only")).is401();
        assertThat(get("/access/admin_only")).is401();
        assertThat(get("/access/public")).is200();
    }
}
