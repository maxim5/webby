package io.webby.netty.response;

import io.netty.handler.codec.http.HttpResponse;
import io.webby.demo.hello.HelloWorld;
import io.webby.netty.HttpConst;
import io.webby.testing.BaseHttpIntegrationTest;
import io.webby.testing.HttpRequestBuilder;
import io.webby.testing.Testing;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashSet;
import java.util.Set;

import static com.google.common.truth.Truth.assertThat;
import static io.webby.testing.AssertResponse.ICON_MIME_TYPES;
import static io.webby.testing.AssertResponse.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class StaticServingIntegrationTest extends BaseHttpIntegrationTest {
    private final StaticServing serving = Testing.testStartup(DEFAULT_SETTINGS).getInstance(StaticServing.class);

    @Test
    public void iterate_static_files() throws Exception {
        Set<String> files = new HashSet<>();
        serving.iterateStaticFiles(files::add);
        assertThat(files).containsAtLeast("favicon.ico", "freemarker/hello.ftl", "jte/example.jte");

        Set<String> dirs = new HashSet<>();
        serving.iterateStaticDirectories(dirs::add);
        assertThat(dirs).containsAtLeast("", "freemarker", "jte", "rocker", "rocker/views");
    }

    @Test
    public void serve_exists() throws Exception {
        assertFavicon(serving.serve("favicon.ico", HttpRequestBuilder.get("/favicon.ico").full()));
    }

    @Test
    public void serve_not_exists() throws Exception {
        assertThat(serving.serve("not-exists.xml", HttpRequestBuilder.get("/not-exists.xml").full())).is404();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void routing_integrates_with_hello_world_handler_default_prefix(boolean dynamicLookup) {
        testSetup(HelloWorld.class, settings -> {
            settings.setProperty("testing.logging", "io.webby.url.impl.HandlerBinder=DEBUG");
            settings.setProperty("url.static.files.prefix", "/");
            settings.setProperty("url.static.files.dynamic.lookup", dynamicLookup);
        }).initHandler();

        assertThat(get("/")).is200().hasContent("Hello World!");
        assertThat(get("/int/10")).is200().hasContent("Hello int <b>10</b>!");
        assertThat(get("/intstr/foo/10")).is200().hasContent("Hello int/str <b>foo</b> and <b>10</b>!");

        assertFavicon(get("/favicon.ico"));
        assertThat(get("/freemarker/hello.ftl")).is200();
        assertThat(get("/jte/example.jte")).is200();

        assertThat(get("/foo/favicon.ico")).is404();
        assertThat(get("/not-exists.xml")).is404();
        assertThat(get("/int")).is404();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void routing_integrates_with_hello_world_handler_custom_prefix(boolean dynamicLookup) {
        testSetup(HelloWorld.class, settings -> {
            settings.setProperty("testing.logging", "io.webby.url.impl.HandlerBinder=DEBUG");
            settings.setProperty("url.static.files.prefix", "/foo/");
            settings.setProperty("url.static.files.dynamic.lookup", dynamicLookup);
        }).initHandler();

        assertThat(get("/")).is200().hasContent("Hello World!");
        assertThat(get("/int/10")).is200().hasContent("Hello int <b>10</b>!");
        assertThat(get("/intstr/foo/10")).is200().hasContent("Hello int/str <b>foo</b> and <b>10</b>!");

        assertFavicon(get("/foo/favicon.ico"));
        assertThat(get("/foo/freemarker/hello.ftl")).is200();
        assertThat(get("/foo/jte/example.jte")).is200();

        assertThat(get("/favicon.ico")).is404();
        assertThat(get("/foo/not-exists.xml")).is404();
        assertThat(get("/int")).is404();
    }

    private static void assertFavicon(@NotNull HttpResponse response) {
        assertThat(response)
            .is200()
            .hasContentLength(15406)
            .hasHeader(HttpConst.ETAG, "1e5d75d6");
        assertThat(response.headers().get(HttpConst.CONTENT_TYPE)).isIn(ICON_MIME_TYPES);
        assertNotNull(response.headers().getTimeMillis(HttpConst.LAST_MODIFIED));
    }
}
