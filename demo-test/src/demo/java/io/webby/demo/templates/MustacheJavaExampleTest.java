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
import static io.webby.testing.AssertResponse.content;
import static io.webby.testing.TestingBytes.assertEqualsIgnoringNewlines;

@RunWith(Parameterized.class)
public class MustacheJavaExampleTest extends BaseHttpIntegrationTest {
    public MustacheJavaExampleTest(@NotNull TestingRender.Config config) {
        testSetup(MustacheJavaExample.class, config.asSettingsUpdater(), TestingModules.instance(config)).initHandler();
    }

    @Parameterized.Parameters(name = "{index}: {0}")
    public static TestingRender.Config[] configs() {
        return TestingRender.Config.values();
    }

    @Test
    public void get_items() {
        HttpResponse response = get("/templates/mustache-java/hello");
        assertThat(response).is200();
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
        HttpResponse rendered = get("/templates/mustache-java/hello");
        assertThat(rendered).is200();
        assertRenderedStatsHeaderForCurrentConfig(rendered);

        HttpResponse manual = get("/templates/manual/mustache-java/hello");
        assertThat(manual).is200();
        assertSimpleStatsHeaderForCurrentConfig(manual);

        assertThat(rendered).hasSameContent(manual);
        assertHeadersForCurrentConfig(rendered, manual);
    }
}
