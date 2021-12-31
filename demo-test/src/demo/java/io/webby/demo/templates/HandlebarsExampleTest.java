package io.webby.demo.templates;

import io.netty.handler.codec.http.HttpResponse;
import io.webby.testing.BaseHttpIntegrationTest;
import io.webby.testing.TestingModules;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static io.webby.demo.templates.TestingRender.*;
import static io.webby.testing.AssertResponse.*;

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
        assert200(response);
        assertContentContains(response, "<h1> Home </h1>", "My name is Maxim. I am a Software Engineer at Google.");
        assertRenderedStatsHeaderForCurrentConfig(response);
    }

    @Test
    public void get_hello_context() {
        HttpResponse response = get("/templates/handlebars/hello/context");
        assert200(response);
        assertContentContains(response, "<h1> Home </h1>", "My name is Maxim. I am a Software Engineer at Google.");
        assertRenderedStatsHeaderForCurrentConfig(response);
    }

    @Test
    public void get_hello_same_as_manual() {
        HttpResponse rendered = get("/templates/handlebars/hello");
        assert200(rendered);
        assertRenderedStatsHeaderForCurrentConfig(rendered);

        HttpResponse manual = get("/templates/manual/handlebars/hello");
        assert200(manual);
        assertSimpleStatsHeaderForCurrentConfig(manual);

        assertContent(rendered, manual);
        assertHeadersForCurrentConfig(rendered, manual);
    }
}
