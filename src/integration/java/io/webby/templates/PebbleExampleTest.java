package io.webby.templates;

import io.netty.handler.codec.http.FullHttpResponse;
import io.webby.netty.BaseIntegrationTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.webby.AssertRequests.*;

public class PebbleExampleTest extends BaseIntegrationTest {
    @BeforeEach
    void setup() {
        testStartup(PebbleExample.class, settings -> {
            settings.setViewPath("src/examples/resources/web/pebble");
        });
    }

    @Test
    public void get_hello() {
        FullHttpResponse response = get("/templates/pebble/hello");
        assert200(response);
        assertContentContains(response,
                "<title> Home </title>",
                "<p> Welcome to my home page. My name is Maxim.</p>",
                "Copyright 2018"
        );
    }

    @Test
    public void get_hello_same_as_manual() {
        FullHttpResponse rendered = get("/templates/pebble/hello");
        assert200(rendered);
        FullHttpResponse manual = get("/templates/manual/pebble/hello");
        assert200(manual);
        Assertions.assertEquals(manual.content(), rendered.content());
    }

    @Test
    public void get_hello_same_as_manual_bytes() {
        FullHttpResponse rendered = get("/templates/pebble/hello");
        assert200(rendered);
        FullHttpResponse manual = get("/templates/manual/pebble/hello/bytes");
        assert200(manual);
        Assertions.assertEquals(manual.content(), rendered.content());
    }
}
