package io.webby.templates;

import io.netty.handler.codec.http.FullHttpResponse;
import io.webby.netty.BaseIntegrationTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.webby.AssertRequests.*;

public class FreeMarkerExampleTest extends BaseIntegrationTest {
    @BeforeEach
    void setup() {
        testStartup(FreeMarkerExample.class);
    }

    @Test
    public void get_hello() {
        FullHttpResponse response = get("/templates/freemarker/hello");
        assert200(response);
        assertContentContains(response,
                "Welcome Big Joe, our beloved leader!",
                "<a href=\"products/green-mouse.html\">Green Mouse</a>!"
        );
    }

    @Test
    public void get_hello_name() {
        FullHttpResponse response = get("/templates/freemarker/hello/NamE");
        assert200(response);
        assertContentContains(response,
                "Welcome NamE!",
                "<a href=\"products/green-mouse.html\">Green Mouse</a>!"
        );
    }

    @Test
    public void get_hello_same_as_manual() {
        FullHttpResponse rendered = get("/templates/freemarker/hello");
        assert200(rendered);
        FullHttpResponse manual = get("/templates/manual/freemarker/hello");
        assert200(manual);
        Assertions.assertEquals(manual.content(), rendered.content());
    }

    @Test
    public void get_hello_name_same_as_manual() {
        FullHttpResponse rendered = get("/templates/freemarker/hello/NamE");
        assert200(rendered);
        FullHttpResponse manual = get("/templates/manual/freemarker/hello/NamE");
        assert200(manual);
        Assertions.assertEquals(manual.content(), rendered.content());
    }

    @Test
    public void get_hello_name_same_as_manual_bytes() {
        FullHttpResponse rendered = get("/templates/freemarker/hello/NamE");
        assert200(rendered);
        FullHttpResponse manual = get("/templates/manual/freemarker/hello-bytes/NamE");
        assert200(manual);
        Assertions.assertEquals(manual.content(), rendered.content());
    }
}
