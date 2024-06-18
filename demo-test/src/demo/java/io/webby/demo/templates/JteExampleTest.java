package io.webby.demo.templates;

import io.netty.handler.codec.http.HttpResponse;
import io.spbx.webby.app.AppSettings;
import io.webby.demo.DevPaths;
import io.spbx.webby.testing.BaseHttpIntegrationTest;
import io.spbx.webby.testing.TestingModules;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.jupiter.api.Tag;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.function.Consumer;

import static io.webby.demo.templates.TestingRender.assertThat;

@Tag("slow")
@Category(Parameterized.class)
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
                                     "<title>Fancy Title</title>")
            .hasRenderedStatsHeaderForCurrentConfig();
    }

    @Test
    public void get_hello_same_as_manual() {
        HttpResponse rendered = get("/templates/jte/hello");
        assertThat(rendered).is200().hasRenderedStatsHeaderForCurrentConfig();

        HttpResponse manual = get("/templates/manual/jte/hello");
        assertThat(manual).is200().hasSimpleStatsHeaderForCurrentConfig();

        assertThat(rendered).matchesContent(manual).matchesHeadersForCurrentConfig(manual);
    }
}
