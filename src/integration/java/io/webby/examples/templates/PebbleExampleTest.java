package io.webby.examples.templates;

import io.netty.handler.codec.http.HttpResponse;
import io.webby.app.AppSettings;
import io.webby.testing.BaseHttpIntegrationTest;
import io.webby.testing.TestingModules;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.function.Consumer;

import static io.webby.examples.templates.TestingRender.*;
import static io.webby.testing.AssertResponse.*;

@RunWith(Parameterized.class)
public class PebbleExampleTest extends BaseHttpIntegrationTest {
    public PebbleExampleTest(@NotNull TestingRender.Config config) {
        Consumer<AppSettings> consumer = config.asSettingsUpdater().andThen(settings ->
            settings.setViewPath("src/examples/resources/web/pebble")
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
        assert200(response);
        assertContentContains(response,
                "<title> Home </title>",
                "<p> Welcome to my home page. My name is Maxim.</p>",
                "Copyright 2018"
        );
        assertRenderedStatsHeaderForCurrentConfig(response);
    }

    @Test
    public void get_hello_same_as_manual() {
        HttpResponse rendered = get("/templates/pebble/hello");
        assert200(rendered);
        assertRenderedStatsHeaderForCurrentConfig(rendered);

        HttpResponse manual = get("/templates/manual/pebble/hello");
        assert200(manual);
        assertSimpleStatsHeaderForCurrentConfig(manual);

        assertContent(rendered, manual);
        assertHeadersForCurrentConfig(rendered, manual);
    }

    @Test
    public void get_hello_same_as_manual_bytes() {
        HttpResponse rendered = get("/templates/pebble/hello");
        assert200(rendered);
        assertRenderedStatsHeaderForCurrentConfig(rendered);

        HttpResponse manual = get("/templates/manual/pebble/hello/bytes");
        assert200(manual);
        assertSimpleStatsHeaderForCurrentConfig(manual);

        assertContent(rendered, manual);
        assertHeadersForCurrentConfig(rendered, manual);
    }
}
