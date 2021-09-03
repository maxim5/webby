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
public class TrimouExampleTest extends BaseHttpIntegrationTest {
    public TrimouExampleTest(@NotNull TestingRender.Config config) {
        testSetup(TrimouExample.class, config.asSettingsUpdater(), TestingModules.instance(config)).initHandler();
    }

    @Parameterized.Parameters(name = "{index}: {0}")
    public static TestingRender.Config[] configs() {
        return TestingRender.Config.values();
    }

    @Test
    public void get_hello() {
        HttpResponse response = get("/templates/trimou/hello");
        assert200(response);
        assertEqualsIgnoringNewlines(content(response), """
            Total number of items: 4
            The first item is:\s
                    1. Foo (5)
                    2. Bar (15)
                    3. INACTIVE!\s
                    4. Baz (5000)
        
        
        """);
        assertRenderedStatsHeaderForCurrentConfig(response);
    }

    @Test
    public void get_hello_same_as_manual() {
        HttpResponse rendered = get("/templates/trimou/hello");
        assert200(rendered);
        assertRenderedStatsHeaderForCurrentConfig(rendered);

        HttpResponse manual = get("/templates/manual/trimou/hello");
        assert200(manual);
        assertSimpleStatsHeaderForCurrentConfig(manual);

        assertContent(rendered, manual);
        assertHeaders(headersWithoutVolatile(rendered), headersWithoutVolatile(manual));
    }
}
