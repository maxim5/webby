package io.webby.demo.templates;

import io.netty.handler.codec.http.HttpResponse;
import io.webby.testing.BaseHttpIntegrationTest;
import io.webby.testing.TestingModules;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static io.webby.demo.templates.TestingRender.*;
import static io.webby.testing.AssertResponse.assertThat;

@RunWith(Parameterized.class)
public class VelocityExampleTest extends BaseHttpIntegrationTest {
    public VelocityExampleTest(@NotNull TestingRender.Config config) {
        testSetup(VelocityExample.class, config.asSettingsUpdater(), TestingModules.instance(config)).initHandler();
    }

    @Parameterized.Parameters(name = "{index}: {0}")
    public static TestingRender.Config[] configs() {
        return TestingRender.Config.values();
    }

    @Test
    public void get_hello() {
        HttpResponse response = get("/templates/velocity/hello");
        assertThat(response)
            .is200()
            .hasContentWhichContains("<table class=\"gridtable\">", "<td>1)</td>", "<td>foo</td>",
                                     "<td>1.23</td>", "<td>2)</td>", "<td>bar</td>", "<td>100.0</td>");
        assertRenderedStatsHeaderForCurrentConfig(response);
    }

    @Test
    public void get_hello_same_as_manual() {
        HttpResponse rendered = get("/templates/velocity/hello");
        assertThat(rendered).is200();
        assertRenderedStatsHeaderForCurrentConfig(rendered);

        HttpResponse manual = get("/templates/manual/velocity/hello");
        assertThat(manual).is200();
        assertSimpleStatsHeaderForCurrentConfig(manual);

        assertThat(rendered).hasSameContent(manual);
        assertHeadersForCurrentConfig(rendered, manual);
    }
}
