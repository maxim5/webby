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
        assert200(response, "Hello World!\n");
        assertRenderedStatsHeaderForCurrentConfig(response);
    }

    @Test
    public void get_model() {
        HttpResponse response = get("/templates/rocker/hello/model");
        assert200(response, "Hello Model!\n");
        assertRenderedStatsHeaderForCurrentConfig(response);
    }

    @Test
    public void get_manual() {
        assert200(get("/templates/manual/rocker/hello"), "Hello World!\n");
        assert200(get("/templates/manual/rocker/hello/string"), "Hello String!\n");
        assert200(get("/templates/manual/rocker/hello/stream"), "Hello Stream!\n");
        assert200(get("/templates/manual/rocker/hello/bytes"), "Hello Bytes!\n");
    }

    @Test
    public void get_hello_same_as_manual() {
        HttpResponse rendered = get("/templates/rocker/hello");
        assert200(rendered);
        assertRenderedStatsHeaderForCurrentConfig(rendered);

        HttpResponse manual = get("/templates/manual/rocker/hello");
        assert200(manual);
        assertSimpleStatsHeaderForCurrentConfig(manual);

        assertContent(rendered, manual);
        assertHeaders(headersWithoutVolatile(rendered), headersWithoutVolatile(manual));
    }
}
