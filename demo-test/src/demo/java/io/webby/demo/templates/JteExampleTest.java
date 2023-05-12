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
public class JteExampleTest extends BaseHttpIntegrationTest {
    public JteExampleTest(@NotNull TestingRender.Config config) {
        Consumer<AppSettings> consumer = config.asSettingsUpdater().andThen(settings -> {
            settings.setViewPath(DevPaths.DEMO_WEB + "jte");
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
        assertThat(response)
            .is200()
            .hasContentWhichContains("<meta name=\"description\" content=\"Fancy Description\">",
                                     "<title>Fancy Title</title>");
        assertThat(response).hasRenderedStatsHeaderForCurrentConfig();
    }

    @Test
    public void get_hello_same_as_manual() {
        HttpResponse rendered = get("/templates/jte/hello");
        assertThat(rendered).is200();
        assertThat(rendered).hasRenderedStatsHeaderForCurrentConfig();

        HttpResponse manual = get("/templates/manual/jte/hello");
        assertThat(manual).is200();
        assertThat(manual).hasSimpleStatsHeaderForCurrentConfig();

        assertThat(rendered).hasSameContent(manual);
        assertThat(rendered).matchesHeadersForCurrentConfig(manual);
    }
}
