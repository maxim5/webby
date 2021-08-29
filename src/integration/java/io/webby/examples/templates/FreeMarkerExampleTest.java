package io.webby.examples.templates;

import io.netty.handler.codec.http.HttpResponse;
import io.webby.testing.BaseHttpIntegrationTest;
import org.junit.jupiter.api.Test;

import static io.webby.testing.AssertResponse.*;

public class FreeMarkerExampleTest extends BaseHttpIntegrationTest {
    protected final FreeMarkerExample handler = testSetup(FreeMarkerExample.class).initHandler();

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
        assertContent(rendered, manual);
    }

    @Test
    public void get_hello_name_same_as_manual() {
        HttpResponse rendered = get("/templates/freemarker/hello/NamE");
        assert200(rendered);
        HttpResponse manual = get("/templates/manual/freemarker/hello/NamE");
        assert200(manual);
        assertContent(rendered, manual);
    }

    @Test
    public void get_hello_name_same_as_manual_bytes() {
        HttpResponse rendered = get("/templates/freemarker/hello/NamE");
        assert200(rendered);
        HttpResponse manual = get("/templates/manual/freemarker/hello-bytes/NamE");
        assert200(manual);
        assertContent(rendered, manual);
    }
}
