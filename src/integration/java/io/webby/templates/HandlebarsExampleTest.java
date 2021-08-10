package io.webby.templates;

import io.netty.handler.codec.http.HttpResponse;
import io.webby.netty.BaseHttpIntegrationTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.webby.AssertResponse.*;

public class HandlebarsExampleTest extends BaseHttpIntegrationTest {
    @BeforeEach
    void setup() {
        testStartup(HandlebarsExample.class);
    }

    @Test
    public void get_hello() {
        HttpResponse response = get("/templates/handlebars/hello");
        assert200(response);
        assertContentContains(response, "<h1> Home </h1>", "My name is Maxim. I am a Software Engineer at Google.");
    }

    @Test
    public void get_hello_context() {
        HttpResponse response = get("/templates/handlebars/hello/context");
        assert200(response);
        assertContentContains(response, "<h1> Home </h1>", "My name is Maxim. I am a Software Engineer at Google.");
    }

    @Test
    public void get_hello_same_as_manual() {
        HttpResponse rendered = get("/templates/handlebars/hello");
        assert200(rendered);
        HttpResponse manual = get("/templates/manual/handlebars/hello");
        assert200(manual);
        Assertions.assertEquals(content(manual), content(rendered));
    }
}
