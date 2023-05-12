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
        assertThat(response)
            .is200()
            .hasContentIgnoringNewlines("""
                Total number of items: 4
                The first item is:\s
                        1. Foo (5)
                        2. Bar (15)
                        3. INACTIVE!\s
                        4. Baz (5000)
            
            
            """);
        assertThat(response).hasRenderedStatsHeaderForCurrentConfig();
    }

    @Test
    public void get_hello_same_as_manual() {
        HttpResponse rendered = get("/templates/trimou/hello");
        assertThat(rendered).is200().hasRenderedStatsHeaderForCurrentConfig();

        HttpResponse manual = get("/templates/manual/trimou/hello");
        assertThat(manual).is200().hasSimpleStatsHeaderForCurrentConfig();

        assertThat(rendered).matchesContent(manual).matchesHeadersForCurrentConfig(manual);
    }
}
