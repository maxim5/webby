package io.webby.templates;

import io.netty.handler.codec.http.FullHttpResponse;
import io.webby.netty.BaseIntegrationTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.webby.AssertRequests.*;

public class JteExampleTest extends BaseIntegrationTest {
    @BeforeEach
    void setup() {
        testStartup(JteExample.class, settings -> {
            settings.setViewPath("src/examples/resources/web/jte");
            settings.setProperty("jte.class.directory", JteExample.CLASS_DIR);
        });
    }

    @Test
    public void get_hello() {
        FullHttpResponse response = get("/templates/jte/hello");
        assert200(response);
        assertContentContains(response,
                "<meta name=\"description\" content=\"Fancy Description\">",
                "<title>Fancy Title</title>"
        );
    }

    @Test
    public void get_hello_same_as_manual() {
        FullHttpResponse rendered = get("/templates/jte/hello");
        assert200(rendered);
        FullHttpResponse manual = get("/templates/manual/jte/hello");
        assert200(manual);
        Assertions.assertEquals(manual.content(), rendered.content());
    }
}
