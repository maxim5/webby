package io.spbx.webby.demo.hello;

import io.spbx.webby.testing.BaseHttpIntegrationTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static io.spbx.webby.testing.AssertResponse.assertThat;

public class ReturnMiscIntegrationTest extends BaseHttpIntegrationTest {
    protected final ReturnMisc handler = testSetup(ReturnMisc.class).initHandler();

    @Test
    public void get_misc() {
        assertThat(get("/r/void")).is200().hasEmptyContent();
        assertThat(get("/r/null")).is200().hasEmptyContent();
    }

    @Test
    public void get_forced_failure() {
        assertThat(get("/r/error/npe")).is500();
    }

    @Test
    @Disabled("Streaming does not allow to change 200 to 500")
    public void get_return_stream_forced_failure() {
        assertThat(get("/r/error/stream/read")).is500();
        assertThat(get("/r/error/stream/close")).is500();
    }

    @Test
    public void get_return_reader_forced_failure() {
        assertThat(get("/r/error/reader/read")).is500();
        assertThat(get("/r/error/reader/close")).is500();
    }
}
