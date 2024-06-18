package io.webby.demo.templates;

import io.netty.handler.codec.http.HttpResponse;
import io.spbx.webby.testing.BaseHttpIntegrationTest;
import io.spbx.webby.testing.TestingModules;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static io.webby.demo.templates.TestingRender.assertThat;

@Category(Parameterized.class)
@RunWith(Parameterized.class)
public class JMustacheExampleTest extends BaseHttpIntegrationTest {
    public JMustacheExampleTest(@NotNull TestingRender.Config config) {
        testSetup(JMustacheExample.class, config.asSettingsUpdater(), TestingModules.instance(config)).initHandler();
    }

    @Parameterized.Parameters(name = "{index}: {0}")
    public static TestingRender.Config[] configs() {
        return TestingRender.Config.values();
    }

    @Test
    public void get_persons() {
        HttpResponse response = get("/templates/jmustache/hello");
        assertThat(response)
            .is200()
            .hasContentIgnoringNewlines("""
                Elvis: 75
                Madonna: 52
                """)
            .hasRenderedStatsHeaderForCurrentConfig();
    }

    @Test
    public void get_persons_same_as_manual() {
        HttpResponse rendered = get("/templates/jmustache/hello");
        assertThat(rendered).is200().hasRenderedStatsHeaderForCurrentConfig();

        HttpResponse manual = get("/templates/manual/jmustache/hello");
        assertThat(manual).is200().hasSimpleStatsHeaderForCurrentConfig();

        assertThat(rendered).matchesContent(manual).matchesHeadersForCurrentConfig(manual);
    }
}
