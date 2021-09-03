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
        assert200(response);
        assertEqualsIgnoringNewlines(content(response), """
        Elvis: 75
        Madonna: 52
        """);
        assertRenderedStatsHeaderForCurrentConfig(response);
    }

    @Test
    public void get_persons_same_as_manual() {
        HttpResponse rendered = get("/templates/jmustache/hello");
        assert200(rendered);
        assertRenderedStatsHeaderForCurrentConfig(rendered);

        HttpResponse manual = get("/templates/manual/jmustache/hello");
        assert200(manual);
        assertSimpleStatsHeaderForCurrentConfig(manual);

        assertContent(rendered, manual);
        assertHeaders(headersWithoutVolatile(rendered), headersWithoutVolatile(manual));
    }
}
