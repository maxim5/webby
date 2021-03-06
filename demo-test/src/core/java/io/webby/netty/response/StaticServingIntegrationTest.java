package io.webby.netty.response;

import io.netty.handler.codec.http.HttpResponse;
import io.webby.demo.hello.HelloWorld;
import io.webby.netty.HttpConst;
import io.webby.testing.BaseHttpIntegrationTest;
import io.webby.testing.FakeRequests;
import io.webby.testing.Testing;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashSet;
import java.util.Set;

import static com.google.common.truth.Truth.assertThat;
import static io.webby.testing.AssertBasics.assertOneOf;
import static io.webby.testing.AssertResponse.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
        assertFavicon(serving.serve("favicon.ico", FakeRequests.get("/favicon.ico")));
    }

    @Test
    public void serve_not_exists() throws Exception {
        assert404(serving.serve("not-exists.xml", FakeRequests.get("/not-exists.xml")));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void routing_integrates_with_hello_world_handler_default_prefix(boolean dynamicLookup) {
        testSetup(HelloWorld.class, settings -> {
            settings.setProperty("testing.logging", "io.webby.url.impl.HandlerBinder=DEBUG");
            settings.setProperty("url.static.files.prefix", "/");
            settings.setProperty("url.static.files.dynamic.lookup", dynamicLookup);
        }).initHandler();

        assert200(get("/"), "Hello World!");
        assert200(get("/int/10"), "Hello int <b>10</b>!");
        assert200(get("/intstr/foo/10"), "Hello int/str <b>foo</b> and <b>10</b>!");

        assertFavicon(get("/favicon.ico"));
        assert200(get("/freemarker/hello.ftl"));
        assert200(get("/jte/example.jte"));

        assert404(get("/foo/favicon.ico"));
        assert404(get("/not-exists.xml"));
        assert404(get("/int"));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void routing_integrates_with_hello_world_handler_custom_prefix(boolean dynamicLookup) {
        testSetup(HelloWorld.class, settings -> {
            settings.setProperty("testing.logging", "io.webby.url.impl.HandlerBinder=DEBUG");
            settings.setProperty("url.static.files.prefix", "/foo/");
            settings.setProperty("url.static.files.dynamic.lookup", dynamicLookup);
        }).initHandler();

        assert200(get("/"), "Hello World!");
        assert200(get("/int/10"), "Hello int <b>10</b>!");
        assert200(get("/intstr/foo/10"), "Hello int/str <b>foo</b> and <b>10</b>!");

        assertFavicon(get("/foo/favicon.ico"));
        assert200(get("/foo/freemarker/hello.ftl"));
        assert200(get("/foo/jte/example.jte"));

        assert404(get("/favicon.ico"));
        assert404(get("/foo/not-exists.xml"));
        assert404(get("/int"));
    }

    private static void assertFavicon(@NotNull HttpResponse response) {
        assert200(response);
        assertEquals("15406", response.headers().get(HttpConst.CONTENT_LENGTH));
        assertOneOf(response.headers().get(HttpConst.CONTENT_TYPE), ICON_MIME_TYPES);
        assertEquals("1e5d75d6", response.headers().get(HttpConst.ETAG));
        assertNotNull(response.headers().getTimeMillis(HttpConst.LAST_MODIFIED));
    }
}
