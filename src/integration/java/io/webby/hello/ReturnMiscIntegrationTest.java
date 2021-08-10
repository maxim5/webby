package io.webby.hello;

import io.webby.testing.BaseHttpIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static io.webby.testing.AssertResponse.*;

public class ReturnMiscIntegrationTest extends BaseHttpIntegrationTest {
    @BeforeEach
    void setup() {
        testStartup(ReturnMisc.class);
    }

    @Test
    public void get_misc() {
        assert200(get("/r/void"), "");
        assert200(get("/r/null"), "");
    }

    @Test
    public void get_forced_failure() {
        assert500(get("/r/error/npe"));
    }

    @Test
    @Disabled("Streaming does not allow to change 200 to 500")
    public void get_return_stream_forced_failure() {
        assert500(get("/r/error/stream/read"));
        assert500(get("/r/error/stream/close"));
    }

    @Test
    public void get_return_reader_forced_failure() {
        assert500(get("/r/error/reader/read"));
        assert500(get("/r/error/reader/close"));
    }
}
