package io.webby.demo.templates;

import io.netty.handler.codec.http.HttpResponse;
import io.webby.testing.BaseHttpIntegrationTest;
import io.webby.testing.TestingModules;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static io.webby.demo.templates.TestingRender.assertThat;

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
                """);
        assertThat(response).hasRenderedStatsHeaderForCurrentConfig();
    }

    @Test
    public void get_persons_same_as_manual() {
        HttpResponse rendered = get("/templates/jmustache/hello");
        assertThat(rendered).is200();
        assertThat(rendered).hasRenderedStatsHeaderForCurrentConfig();

        HttpResponse manual = get("/templates/manual/jmustache/hello");
        assertThat(manual).is200();
        assertThat(manual).hasSimpleStatsHeaderForCurrentConfig();

        assertThat(rendered).hasSameContent(manual);
        assertThat(rendered).matchesHeadersForCurrentConfig(manual);
    }
}
