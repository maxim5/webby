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
        assert200(response);
        assertContentContains(response,
            "<table class=\"gridtable\">",
            "<td>1)</td>", "<td>foo</td>", "<td>1.23</td>",
            "<td>2)</td>", "<td>bar</td>", "<td>100.0</td>"
        );
        assertRenderedStatsHeaderForCurrentConfig(response);
    }

    @Test
    public void get_hello_same_as_manual() {
        HttpResponse rendered = get("/templates/velocity/hello");
        assert200(rendered);
        assertRenderedStatsHeaderForCurrentConfig(rendered);

        HttpResponse manual = get("/templates/manual/velocity/hello");
        assert200(manual);
        assertSimpleStatsHeaderForCurrentConfig(manual);

        assertContent(rendered, manual);
        assertHeadersForCurrentConfig(rendered, manual);
    }
}
