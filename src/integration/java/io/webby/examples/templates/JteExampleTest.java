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

import static io.webby.examples.templates.TestingRender.assertRenderedStatsHeaderForCurrentConfig;
import static io.webby.examples.templates.TestingRender.assertSimpleStatsHeaderForCurrentConfig;
import static io.webby.testing.AssertResponse.*;

@RunWith(Parameterized.class)
public class JteExampleTest extends BaseHttpIntegrationTest {
    public JteExampleTest(@NotNull TestingRender.Config config) {
        Consumer<AppSettings> consumer = config.asSettingsUpdater().andThen(settings -> {
            settings.setViewPath("src/examples/resources/web/jte");
            settings.setProperty("jte.class.directory", JteExample.CLASS_DIR);
        });
        testSetup(JteExample.class, consumer, TestingModules.instance(config)).initHandler();
    }

    @Parameterized.Parameters(name = "{index}: {0}")
    public static TestingRender.Config[] configs() {
        return TestingRender.Config.values();
    }

    @Test
    public void get_hello() {
        HttpResponse response = get("/templates/jte/hello");
        assert200(response);
        assertContentContains(response,
                "<meta name=\"description\" content=\"Fancy Description\">",
                "<title>Fancy Title</title>"
        );
        assertRenderedStatsHeaderForCurrentConfig(response);
    }

    @Test
    public void get_hello_same_as_manual() {
        HttpResponse rendered = get("/templates/jte/hello");
        assert200(rendered);
        assertRenderedStatsHeaderForCurrentConfig(rendered);

        HttpResponse manual = get("/templates/manual/jte/hello");
        assert200(manual);
        assertSimpleStatsHeaderForCurrentConfig(manual);

        assertContent(rendered, manual);
        assertHeaders(headersWithoutVolatile(rendered), headersWithoutVolatile(manual));
    }
}
