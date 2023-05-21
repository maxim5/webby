package io.webby.demo.templates;

import io.netty.handler.codec.http.HttpResponse;
import io.webby.app.AppSettings;
import io.webby.demo.DevPaths;
import io.webby.testing.BaseHttpIntegrationTest;
import io.webby.testing.TestingModules;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.function.Consumer;

import static io.webby.demo.templates.TestingRender.assertThat;

@RunWith(Parameterized.class)
public class PebbleExampleTest extends BaseHttpIntegrationTest {
    public PebbleExampleTest(@NotNull TestingRender.Config config) {
        Consumer<AppSettings> consumer = config.asSettingsUpdater().andThen(settings ->
            settings.setViewPath(DevPaths.DEMO_WEB + "pebble")
        );
        testSetup(PebbleExample.class, consumer, TestingModules.instance(config)).initHandler();
    }

    @Parameterized.Parameters(name = "{index}: {0}")
    public static TestingRender.Config[] configs() {
        return TestingRender.Config.values();
    }

    @Test
    public void get_hello() {
        HttpResponse response = get("/templates/pebble/hello");
        assertThat(response)
            .is200()
            .hasContentWhichContains("<title> Home </title>",
                                     "<p> Welcome to my home page. My name is Maxim.</p>",
                                     "Copyright 2018")
            .hasRenderedStatsHeaderForCurrentConfig();
    }

    @Test
    public void get_hello_same_as_manual() {
        HttpResponse rendered = get("/templates/pebble/hello");
        assertThat(rendered).is200().hasRenderedStatsHeaderForCurrentConfig();

        HttpResponse manual = get("/templates/manual/pebble/hello");
        assertThat(manual).is200().hasSimpleStatsHeaderForCurrentConfig();

        assertThat(rendered).matchesContent(manual).matchesHeadersForCurrentConfig(manual);
    }

    @Test
    public void get_hello_same_as_manual_bytes() {
        HttpResponse rendered = get("/templates/pebble/hello");
        assertThat(rendered).is200().hasRenderedStatsHeaderForCurrentConfig();

        HttpResponse manual = get("/templates/manual/pebble/hello/bytes");
        assertThat(manual).is200().hasSimpleStatsHeaderForCurrentConfig();

        assertThat(rendered).matchesContent(manual).matchesHeadersForCurrentConfig(manual);
    }
}
