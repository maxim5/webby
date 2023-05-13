package io.webby.netty.dispatch.http;

import com.google.inject.Injector;
import io.webby.app.AppSettings;
import io.webby.testing.Testing;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NettyHttpStepNavigatorTest {
    private final Injector injector = Testing.testStartup();
    private final NettyHttpStepNavigator handler = injector.getInstance(NettyHttpStepNavigator.class);

    @Test
    public void extractPath_simple() {
        assertEquals("", handler.extractPath("").toString());
        assertEquals("foo", handler.extractPath("foo").toString());
        assertEquals("foo-bar", handler.extractPath("foo-bar").toString());

        assertEquals("/", handler.extractPath("/").toString());
        assertEquals("///", handler.extractPath("///").toString());

        assertEquals("/foo", handler.extractPath("/foo").toString());
        assertEquals("/foo/bar", handler.extractPath("/foo/bar").toString());
        assertEquals("/foo/bar/", handler.extractPath("/foo/bar/").toString());

        assertEquals("", handler.extractPath("?").toString());
        assertEquals("", handler.extractPath("#").toString());
        assertEquals("/", handler.extractPath("/?").toString());
        assertEquals("/", handler.extractPath("/?key=value").toString());

        assertEquals("/foo", handler.extractPath("/foo?").toString());
        assertEquals("/foo", handler.extractPath("/foo?key=value").toString());
        assertEquals("/foo", handler.extractPath("/foo?key=value&key=").toString());
        assertEquals("/foo", handler.extractPath("/foo?&=").toString());

        assertEquals("/", handler.extractPath("/#").toString());
        assertEquals("/", handler.extractPath("/#ignore").toString());
        assertEquals("/", handler.extractPath("/?#").toString());
        assertEquals("/", handler.extractPath("/?key=value#").toString());
    }

    @Test
    public void extractPath_trailingSlash() {
        AppSettings appSettings = injector.getInstance(AppSettings.class);
        appSettings.setProperty("netty.url.trailing.slash.ignore", true);

        assertEquals("a", handler.extractPath("a").toString());
        assertEquals("foo", handler.extractPath("foo").toString());

        assertEquals("/", handler.extractPath("/").toString());
        assertEquals("/", handler.extractPath("//").toString());
        assertEquals("/foo", handler.extractPath("/foo").toString());
        assertEquals("/foo", handler.extractPath("/foo/").toString());
        assertEquals("/foo/", handler.extractPath("/foo//").toString());

        assertEquals("/foo/bar", handler.extractPath("/foo/bar").toString());
        assertEquals("/foo/bar", handler.extractPath("/foo/bar/").toString());
    }
}
