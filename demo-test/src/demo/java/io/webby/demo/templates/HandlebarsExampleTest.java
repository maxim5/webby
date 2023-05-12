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
public class HandlebarsExampleTest extends BaseHttpIntegrationTest {
    public HandlebarsExampleTest(@NotNull TestingRender.Config config) {
        testSetup(HandlebarsExample.class, config.asSettingsUpdater(), TestingModules.instance(config)).initHandler();
    }

    @Parameterized.Parameters(name = "{index}: {0}")
    public static TestingRender.Config[] configs() {
        return TestingRender.Config.values();
    }

    @Test
    public void get_hello() {
        HttpResponse response = get("/templates/handlebars/hello");
        assertThat(response)
            .is200()
            .hasContentWhichContains("<h1> Home </h1>", "My name is Maxim. I am a Software Engineer at Google.");
        assertThat(response).hasRenderedStatsHeaderForCurrentConfig();
    }

    @Test
    public void get_hello_context() {
        HttpResponse response = get("/templates/handlebars/hello/context");
        assertThat(response)
            .is200()
            .hasContentWhichContains("<h1> Home </h1>", "My name is Maxim. I am a Software Engineer at Google.");
        assertThat(response).hasRenderedStatsHeaderForCurrentConfig();
    }

    @Test
    public void get_hello_same_as_manual() {
        HttpResponse rendered = get("/templates/handlebars/hello");
        assertThat(rendered).is200();
        assertThat(rendered).hasRenderedStatsHeaderForCurrentConfig();

        HttpResponse manual = get("/templates/manual/handlebars/hello");
        assertThat(manual).is200();
        assertThat(manual).hasSimpleStatsHeaderForCurrentConfig();

        assertThat(rendered).hasSameContent(manual);
        assertThat(rendered).matchesHeadersForCurrentConfig(manual);
    }
}
