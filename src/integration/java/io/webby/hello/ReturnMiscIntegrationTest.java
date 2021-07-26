package io.webby.hello;

import io.webby.netty.BaseIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ReturnMiscIntegrationTest extends BaseIntegrationTest {
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
        assert503(get("/r/error/npe"));
    }

    @Test
    public void get_return_stream_forced_failure() {
        assert503(get("/r/error/stream/read"));
        assert503(get("/r/error/stream/close"));
    }

    @Test
    public void get_return_reader_forced_failure() {
        assert503(get("/r/error/reader/read"));
        assert503(get("/r/error/reader/close"));
    }
}
