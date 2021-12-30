package io.webby.examples.templates;

import io.netty.handler.codec.http.HttpResponse;
import io.webby.testing.BaseHttpIntegrationTest;
import io.webby.testing.TestingModules;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static io.webby.examples.templates.TestingRender.*;
import static io.webby.testing.AssertResponse.*;
import static io.webby.testing.TestingBytes.assertEqualsIgnoringNewlines;

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
        assert200(response);
        assertEqualsIgnoringNewlines(content(response), """
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
        assert200(rendered);
        assertRenderedStatsHeaderForCurrentConfig(rendered);

        HttpResponse manual = get("/templates/manual/thymeleaf/hello");
        assert200(manual);
        assertSimpleStatsHeaderForCurrentConfig(manual);

        assertContent(rendered, manual);
        assertHeadersForCurrentConfig(rendered, manual);
    }
}
