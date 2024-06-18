package io.webby.demo.templates;

import io.netty.handler.codec.http.HttpResponse;
import io.spbx.webby.testing.BaseHttpIntegrationTest;
import io.spbx.webby.testing.TestingModules;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static io.webby.demo.templates.TestingRender.assertThat;

@Category(Parameterized.class)
@RunWith(Parameterized.class)
public class ThymeleafExampleTest extends BaseHttpIntegrationTest {
    public ThymeleafExampleTest(@NotNull TestingRender.Config config) {
        testSetup(ThymeleafExample.class, config.asSettingsUpdater(), TestingModules.instance(config)).initHandler();
    }

    @Parameterized.Parameters(name = "{index}: {0}")
    public static TestingRender.Config[] configs() {
        return TestingRender.Config.values();
    }

    @Test
    public void get_hello() {
        HttpResponse response = get("/templates/thymeleaf/hello");
        assertThat(response)
            .is200()
            .hasContentIgnoringNewlines("""
                <html>
                    <body>
                        <table>
                            <tr>
                                <td >101</td>
                                <td >Maxim</td>
                                <td>
                                    <span >Male</span>
                                   \s
                                </td>
                            </tr>
                        </table>
                    </body>
                </html>
                """)
            .hasRenderedStatsHeaderForCurrentConfig();
    }

    @Test
    public void get_hello_same_as_manual() {
        HttpResponse rendered = get("/templates/thymeleaf/hello");
        assertThat(rendered).is200().hasRenderedStatsHeaderForCurrentConfig();

        HttpResponse manual = get("/templates/manual/thymeleaf/hello");
        assertThat(manual).is200().hasSimpleStatsHeaderForCurrentConfig();

        assertThat(rendered).matchesContent(manual).matchesHeadersForCurrentConfig(manual);
    }
}
