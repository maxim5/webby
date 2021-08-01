package io.webby.templates;

import io.netty.handler.codec.http.HttpResponse;
import io.webby.netty.BaseIntegrationTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.webby.AssertResponse.*;

public class FreeMarkerExampleTest extends BaseIntegrationTest {
    @BeforeEach
    void setup() {
        testStartup(FreeMarkerExample.class);
    }

    @Test
    public void get_hello() {
        HttpResponse response = get("/templates/freemarker/hello");
        assert200(response);
        assertContentContains(response,
                "Welcome Big Joe, our beloved leader!",
                "<a href=\"products/green-mouse.html\">Green Mouse</a>!"
        );
    }

    @Test
    public void get_hello_name() {
        HttpResponse response = get("/templates/freemarker/hello/NamE");
        assert200(response);
        assertContentContains(response,
                "Welcome NamE!",
                "<a href=\"products/green-mouse.html\">Green Mouse</a>!"
        );
    }

    @Test
    public void get_hello_same_as_manual() {
        HttpResponse rendered = get("/templates/freemarker/hello");
        assert200(rendered);
        HttpResponse manual = get("/templates/manual/freemarker/hello");
        assert200(manual);
        Assertions.assertEquals(content(manual), content(rendered));
    }

    @Test
    public void get_hello_name_same_as_manual() {
        HttpResponse rendered = get("/templates/freemarker/hello/NamE");
        assert200(rendered);
        HttpResponse manual = get("/templates/manual/freemarker/hello/NamE");
        assert200(manual);
        Assertions.assertEquals(content(manual), content(rendered));
    }

    @Test
    public void get_hello_name_same_as_manual_bytes() {
        HttpResponse rendered = get("/templates/freemarker/hello/NamE");
        assert200(rendered);
        HttpResponse manual = get("/templates/manual/freemarker/hello-bytes/NamE");
        assert200(manual);
        Assertions.assertEquals(content(manual), content(rendered));
    }
}
