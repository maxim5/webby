package io.webby.templates;

import io.netty.handler.codec.http.FullHttpResponse;
import io.webby.netty.BaseIntegrationTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class RockerExampleTest extends BaseIntegrationTest {
    @BeforeEach
    void setup() {
        testStartup(RockerExample.class);
    }

    @Test
    public void get_bound_template() {
        assert200(get("/templates/rocker/hello"), "Hello World!\n");
    }

    @Test
    public void get_model() {
        assert200(get("/templates/rocker/hello/model"), "Hello Model!\n");
    }

    @Test
    public void get_manual() {
        assert200(get("/templates/manual/rocker/hello"), "Hello World!\n");
        assert200(get("/templates/manual/rocker/hello/string"), "Hello String!\n");
        assert200(get("/templates/manual/rocker/hello/stream"), "Hello Stream!\n");
        assert200(get("/templates/manual/rocker/hello/bytes"), "Hello Bytes!\n");
    }

    @Test
    public void get_hello_same_as_manual() {
        FullHttpResponse rendered = get("/templates/rocker/hello");
        assert200(rendered);
        FullHttpResponse manual = get("/templates/manual/rocker/hello");
        assert200(manual);
        Assertions.assertEquals(manual.content(), rendered.content());
    }
}
