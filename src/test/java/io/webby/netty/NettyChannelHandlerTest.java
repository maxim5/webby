package io.webby.netty;

import com.google.inject.Injector;
import io.webby.Testing;
import io.webby.app.AppSettings;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class NettyChannelHandlerTest {
    private final Injector injector = Testing.testStartupNoHandlers();
    private final NettyChannelHandler handler = injector.getInstance(NettyChannelHandler.class);

    @Test
    public void extractPath() {
        Assertions.assertEquals("", handler.extractPath("").toString());
        Assertions.assertEquals("foo", handler.extractPath("foo").toString());
        Assertions.assertEquals("foo-bar", handler.extractPath("foo-bar").toString());

        Assertions.assertEquals("/", handler.extractPath("/").toString());
        Assertions.assertEquals("///", handler.extractPath("///").toString());

        Assertions.assertEquals("/foo", handler.extractPath("/foo").toString());
        Assertions.assertEquals("/foo/bar", handler.extractPath("/foo/bar").toString());
        Assertions.assertEquals("/foo/bar/", handler.extractPath("/foo/bar/").toString());

        Assertions.assertEquals("", handler.extractPath("?").toString());
        Assertions.assertEquals("", handler.extractPath("#").toString());
        Assertions.assertEquals("/", handler.extractPath("/?").toString());
        Assertions.assertEquals("/", handler.extractPath("/?key=value").toString());

        Assertions.assertEquals("/foo", handler.extractPath("/foo?").toString());
        Assertions.assertEquals("/foo", handler.extractPath("/foo?key=value").toString());
        Assertions.assertEquals("/foo", handler.extractPath("/foo?key=value&key=").toString());
        Assertions.assertEquals("/foo", handler.extractPath("/foo?&=").toString());

        Assertions.assertEquals("/", handler.extractPath("/#").toString());
        Assertions.assertEquals("/", handler.extractPath("/#ignore").toString());
        Assertions.assertEquals("/", handler.extractPath("/?#").toString());
        Assertions.assertEquals("/", handler.extractPath("/?key=value#").toString());
    }

    @Test
    public void extractPath_trailingSlash() {
        AppSettings appSettings = injector.getInstance(AppSettings.class);
        appSettings.setProperty("netty.url.trailing.slash.ignore", true);

        Assertions.assertEquals("a", handler.extractPath("a").toString());
        Assertions.assertEquals("foo", handler.extractPath("foo").toString());

        Assertions.assertEquals("/", handler.extractPath("/").toString());
        Assertions.assertEquals("/", handler.extractPath("//").toString());
        Assertions.assertEquals("/foo", handler.extractPath("/foo").toString());
        Assertions.assertEquals("/foo", handler.extractPath("/foo/").toString());
        Assertions.assertEquals("/foo/", handler.extractPath("/foo//").toString());

        Assertions.assertEquals("/foo/bar", handler.extractPath("/foo/bar").toString());
        Assertions.assertEquals("/foo/bar", handler.extractPath("/foo/bar/").toString());
    }
}
