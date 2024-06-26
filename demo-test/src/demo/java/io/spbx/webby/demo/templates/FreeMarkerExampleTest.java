package io.spbx.webby.demo.templates;

import io.netty.handler.codec.http.HttpResponse;
import io.spbx.webby.testing.BaseHttpIntegrationTest;
import io.spbx.webby.testing.TestingModules;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static io.spbx.webby.demo.templates.TestingRender.assertThat;

@Category(Parameterized.class)
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
        assertThat(response)
            .is200()
            .hasContentWhichContains("Welcome Big Joe, our beloved leader!",
                                     "<a href=\"products/green-mouse.html\">Green Mouse</a>!")
            .hasRenderedStatsHeaderForCurrentConfig();
    }

    @Test
    public void get_hello_name() {
        HttpResponse response = get("/templates/freemarker/hello/NamE");
        assertThat(response)
            .is200()
            .hasContentWhichContains("Welcome NamE!", "<a href=\"products/green-mouse.html\">Green Mouse</a>!")
            .hasRenderedStatsHeaderForCurrentConfig();
    }

    @Test
    public void get_hello_same_as_manual() {
        HttpResponse rendered = get("/templates/freemarker/hello");
        assertThat(rendered).is200().hasRenderedStatsHeaderForCurrentConfig();

        HttpResponse manual = get("/templates/manual/freemarker/hello");
        assertThat(manual).is200().hasSimpleStatsHeaderForCurrentConfig();

        assertThat(rendered).matchesContent(manual).matchesHeadersForCurrentConfig(manual);
    }

    @Test
    public void get_hello_name_same_as_manual() {
        HttpResponse rendered = get("/templates/freemarker/hello/NamE");
        assertThat(rendered).is200().hasRenderedStatsHeaderForCurrentConfig();

        HttpResponse manual = get("/templates/manual/freemarker/hello/NamE");
        assertThat(manual).is200().hasSimpleStatsHeaderForCurrentConfig();

        assertThat(rendered).matchesContent(manual).matchesHeadersForCurrentConfig(manual);
    }

    @Test
    public void get_hello_name_same_as_manual_bytes() {
        HttpResponse rendered = get("/templates/freemarker/hello/NamE");
        assertThat(rendered).is200().hasRenderedStatsHeaderForCurrentConfig();

        HttpResponse manual = get("/templates/manual/freemarker/hello-bytes/NamE");
        assertThat(manual).is200().hasSimpleStatsHeaderForCurrentConfig();

        assertThat(rendered).matchesContent(manual).matchesHeadersForCurrentConfig(manual);
    }
}
