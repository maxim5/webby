package io.webby.examples.templates;

import io.netty.handler.codec.http.HttpResponse;
import io.webby.testing.BaseHttpIntegrationTest;
import io.webby.testing.TestingModules;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static io.webby.examples.templates.TestingRender.assertRenderedStatsHeaderForCurrentConfig;
import static io.webby.examples.templates.TestingRender.assertSimpleStatsHeaderForCurrentConfig;
import static io.webby.testing.AssertResponse.*;
import static io.webby.testing.TestingBytes.assertEqualsIgnoringNewlines;

@RunWith(Parameterized.class)
public class MustacheExampleTest extends BaseHttpIntegrationTest {
    public MustacheExampleTest(@NotNull TestingRender.Config config) {
        testSetup(MustacheExample.class, config.asSettingsUpdater(), TestingModules.instance(config)).initHandler();
    }

    @Parameterized.Parameters(name = "{index}: {0}")
    public static TestingRender.Config[] configs() {
        return TestingRender.Config.values();
    }

    @Test
    public void get_items() {
        HttpResponse response = get("/templates/mustache/hello");
        assert200(response);
        assertEqualsIgnoringNewlines(content(response), """
        Name: Foo
        Price: 10.0
          Feature: New!
        Name: Bar
        Price: 99.99
          Feature: Cool
          Feature: Awesome
        Name: Qux
        Price: 1.5
        Name: Baz
        Price: 500.0
          Feature: Old
        """);
        assertRenderedStatsHeaderForCurrentConfig(response);
    }

    @Test
    public void get_items_same_as_manual() {
        HttpResponse rendered = get("/templates/mustache/hello");
        assert200(rendered);
        assertRenderedStatsHeaderForCurrentConfig(rendered);

        HttpResponse manual = get("/templates/manual/mustache/hello");
        assert200(manual);
        assertSimpleStatsHeaderForCurrentConfig(manual);

        assertContent(rendered, manual);
        assertHeaders(headersWithoutVolatile(rendered), headersWithoutVolatile(manual));
    }
}
