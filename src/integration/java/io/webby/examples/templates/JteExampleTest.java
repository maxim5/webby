package io.webby.examples.templates;

import io.netty.handler.codec.http.HttpResponse;
import io.webby.testing.BaseHttpIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.webby.testing.AssertResponse.*;

public class JteExampleTest extends BaseHttpIntegrationTest {
    @BeforeEach
    void setup() {
        testSetup(JteExample.class, settings -> {
            settings.setViewPath("src/examples/resources/web/jte");
            settings.setProperty("jte.class.directory", JteExample.CLASS_DIR);
        });
    }

    @Test
    public void get_hello() {
        HttpResponse response = get("/templates/jte/hello");
        assert200(response);
        assertContentContains(response,
                "<meta name=\"description\" content=\"Fancy Description\">",
                "<title>Fancy Title</title>"
        );
    }

    @Test
    public void get_hello_same_as_manual() {
        HttpResponse rendered = get("/templates/jte/hello");
        assert200(rendered);
        HttpResponse manual = get("/templates/manual/jte/hello");
        assert200(manual);
        assertContent(rendered, manual);
    }
}
