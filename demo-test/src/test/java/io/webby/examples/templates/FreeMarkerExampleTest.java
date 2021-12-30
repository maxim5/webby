package io.webby.examples.templates;

import io.netty.handler.codec.http.HttpResponse;
import io.webby.testing.BaseHttpIntegrationTest;
import io.webby.testing.TestingModules;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static io.webby.examples.templates.TestingRender.*;
import static io.webby.testing.AssertResponse.*;

@RunWith(Parameterized.class)
public class FreeMarkerExampleTest extends BaseHttpIntegrationTest {
    public FreeMarkerExampleTest(@NotNull TestingRender.Config config) {
        testSetup(FreeMarkerExample.class, config.asSettingsUpdater(), TestingModules.instance(config)).initHandler();
    }

    @Parameterized.Parameters(name = "{index}: {0}")
    public static TestingRender.Config[] configs() {
        return TestingRender.Config.values();
    }

    @Test
    public void get_hello() {
        HttpResponse response = get("/templates/freemarker/hello");
        assert200(response);
        assertContentContains(response,
            "Welcome Big Joe, our beloved leader!",
            "<a href=\"products/green-mouse.html\">Green Mouse</a>!"
        );
        assertRenderedStatsHeaderForCurrentConfig(response);
    }

    @Test
    public void get_hello_name() {
        HttpResponse response = get("/templates/freemarker/hello/NamE");
        assert200(response);
        assertContentContains(response,
            "Welcome NamE!",
            "<a href=\"products/green-mouse.html\">Green Mouse</a>!"
        );
        assertRenderedStatsHeaderForCurrentConfig(response);
    }

    @Test
    public void get_hello_same_as_manual() {
        HttpResponse rendered = get("/templates/freemarker/hello");
        assert200(rendered);
        assertRenderedStatsHeaderForCurrentConfig(rendered);

        HttpResponse manual = get("/templates/manual/freemarker/hello");
        assert200(manual);
        assertSimpleStatsHeaderForCurrentConfig(manual);

        assertContent(rendered, manual);
        assertHeadersForCurrentConfig(rendered, manual);
    }

    @Test
    public void get_hello_name_same_as_manual() {
        HttpResponse rendered = get("/templates/freemarker/hello/NamE");
        assert200(rendered);
        assertRenderedStatsHeaderForCurrentConfig(rendered);

        HttpResponse manual = get("/templates/manual/freemarker/hello/NamE");
        assert200(manual);
        assertSimpleStatsHeaderForCurrentConfig(manual);

        assertContent(rendered, manual);
        assertHeadersForCurrentConfig(rendered, manual);
    }

    @Test
    public void get_hello_name_same_as_manual_bytes() {
        HttpResponse rendered = get("/templates/freemarker/hello/NamE");
        assert200(rendered);
        assertRenderedStatsHeaderForCurrentConfig(rendered);

        HttpResponse manual = get("/templates/manual/freemarker/hello-bytes/NamE");
        assert200(manual);
        assertSimpleStatsHeaderForCurrentConfig(manual);

        assertContent(rendered, manual);
        assertHeadersForCurrentConfig(rendered, manual);
    }
}
