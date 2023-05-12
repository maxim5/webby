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
                """);
        assertRenderedStatsHeaderForCurrentConfig(response);
    }

    @Test
    public void get_hello_same_as_manual() {
        HttpResponse rendered = get("/templates/thymeleaf/hello");
        assertThat(rendered).is200();
        assertRenderedStatsHeaderForCurrentConfig(rendered);

        HttpResponse manual = get("/templates/manual/thymeleaf/hello");
        assertThat(manual).is200();
        assertSimpleStatsHeaderForCurrentConfig(manual);

        assertThat(rendered).hasSameContent(manual);
        assertHeadersForCurrentConfig(rendered, manual);
    }
}
