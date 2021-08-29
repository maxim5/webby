package io.webby.examples.templates;

import io.netty.handler.codec.http.HttpResponse;
import io.webby.testing.BaseHttpIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.webby.testing.AssertResponse.*;

public class PebbleExampleTest extends BaseHttpIntegrationTest {
    @BeforeEach
    void setup() {
        testSetup(PebbleExample.class, settings ->
            settings.setViewPath("src/examples/resources/web/pebble")
        );
    }

    @Test
    public void get_hello() {
        HttpResponse response = get("/templates/pebble/hello");
        assert200(response);
        assertContentContains(response,
                "<title> Home </title>",
                "<p> Welcome to my home page. My name is Maxim.</p>",
                "Copyright 2018"
        );
    }

    @Test
    public void get_hello_same_as_manual() {
        HttpResponse rendered = get("/templates/pebble/hello");
        assert200(rendered);
        HttpResponse manual = get("/templates/manual/pebble/hello");
        assert200(manual);
        assertContent(rendered, manual);
    }

    @Test
    public void get_hello_same_as_manual_bytes() {
        HttpResponse rendered = get("/templates/pebble/hello");
        assert200(rendered);
        HttpResponse manual = get("/templates/manual/pebble/hello/bytes");
        assert200(manual);
        assertContent(rendered, manual);
    }
}
