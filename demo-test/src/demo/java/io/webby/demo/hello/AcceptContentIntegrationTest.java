package io.webby.demo.hello;

import io.spbx.webby.testing.BaseHttpIntegrationTest;
import org.junit.jupiter.api.Test;

import static io.spbx.webby.testing.AssertResponse.assertThat;

public class AcceptContentIntegrationTest extends BaseHttpIntegrationTest {
    protected final AcceptContent handler = testSetup(AcceptContent.class).initHandler();

    @Test
    public void post_no_content() {
        assertThat(post("/")).is404();

        assertThat(post("/int/10")).is200().hasContent("{}");
        assertThat(post("/int/0")).is400();
        assertThat(post("/int/-1")).is400();
        assertThat(post("/int/foo")).is400();
    }

    @Test
    public void post_content_object() {
        assertThat(post("/string/foo/10", new int[] {1, 2, 3})).is200()
            .hasContent("Vars: str=foo y=10 content=<[1.0, 2.0, 3.0]>");
    }

    @Test
    public void post_bytebuf() {
        assertThat(post("/content/bytebuf", "foobar".getBytes())).is200().hasContent("len=6");
    }
}
