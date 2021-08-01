package io.webby.templates;

import io.netty.handler.codec.http.HttpResponse;
import io.webby.netty.BaseIntegrationTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.webby.AssertResponse.*;

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
        HttpResponse rendered = get("/templates/rocker/hello");
        assert200(rendered);
        HttpResponse manual = get("/templates/manual/rocker/hello");
        assert200(manual);
        Assertions.assertEquals(fullContent(manual), fullContent(rendered));
    }
}
