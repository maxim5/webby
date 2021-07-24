package io.webby.templates;

import io.netty.handler.codec.http.FullHttpResponse;
import io.webby.netty.BaseIntegrationTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class HandlebarsExampleTest extends BaseIntegrationTest {
    @BeforeEach
    void setup() {
        testStartup(HandlebarsExample.class);
    }

    @Test
    public void get_hello() {
        FullHttpResponse response = get("/templates/handlebars/hello");
        assert200(response);
        assertContentContains(response, "<h1> Home </h1>", "My name is Maxim. I am a Software Engineer at Google.");
    }

    @Test
    public void get_hello_context() {
        FullHttpResponse response = get("/templates/handlebars/hello/context");
        assert200(response);
        assertContentContains(response, "<h1> Home </h1>", "My name is Maxim. I am a Software Engineer at Google.");
    }

    @Test
    public void get_hello_same_as_manual() {
        FullHttpResponse rendered = get("/templates/handlebars/hello");
        assert200(rendered);
        FullHttpResponse manual = get("/templates/manual/handlebars/hello");
        assert200(manual);
        Assertions.assertEquals(manual.content(), rendered.content());
    }
}
