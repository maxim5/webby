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
public class RockerExampleTest extends BaseHttpIntegrationTest {
    public RockerExampleTest(@NotNull TestingRender.Config config) {
        testSetup(RockerExample.class, config.asSettingsUpdater(), TestingModules.instance(config)).initHandler();
    }

    @Parameterized.Parameters(name = "{index}: {0}")
    public static TestingRender.Config[] configs() {
        return TestingRender.Config.values();
    }

    @Test
    public void get_bound_template() {
        HttpResponse response = get("/templates/rocker/hello");
        assertThat(response)
            .is200()
            .hasContent("Hello World!\n")
            .hasRenderedStatsHeaderForCurrentConfig();
    }

    @Test
    public void get_model() {
        HttpResponse response = get("/templates/rocker/hello/model");
        assertThat(response)
            .is200()
            .hasContent("Hello Model!\n")
            .hasRenderedStatsHeaderForCurrentConfig();
    }

    @Test
    public void get_manual() {
        assertThat(get("/templates/manual/rocker/hello")).is200().hasContent("Hello World!\n");
        assertThat(get("/templates/manual/rocker/hello/string")).is200().hasContent("Hello String!\n");
        assertThat(get("/templates/manual/rocker/hello/stream")).is200().hasContent("Hello Stream!\n");
        assertThat(get("/templates/manual/rocker/hello/bytes")).is200().hasContent("Hello Bytes!\n");
    }

    @Test
    public void get_hello_same_as_manual() {
        HttpResponse rendered = get("/templates/rocker/hello");
        assertThat(rendered).is200().hasRenderedStatsHeaderForCurrentConfig();

        HttpResponse manual = get("/templates/manual/rocker/hello");
        assertThat(manual).is200().hasSimpleStatsHeaderForCurrentConfig();

        assertThat(rendered).matchesContent(manual).matchesHeadersForCurrentConfig(manual);
    }
}
