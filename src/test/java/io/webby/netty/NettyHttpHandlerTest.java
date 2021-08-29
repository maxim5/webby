package io.webby.netty;

import com.google.inject.Injector;
import io.webby.app.AppSettings;
import io.webby.testing.Testing;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NettyHttpHandlerTest {
    private final Injector injector = Testing.testStartupNoHandlers();
    private final NettyHttpHandler handler = injector.getInstance(NettyHttpHandler.class);

    @Test
    public void extractPath() {
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
