package io.spbx.webby.netty.dispatch.http;

import com.google.inject.Injector;
import io.spbx.webby.app.AppSettings;
import io.spbx.webby.testing.Testing;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;

@Tag("fast")
public class NettyHttpStepNavigatorTest {
    private final Injector injector = Testing.testStartup();
    private final NettyHttpStepNavigator handler = injector.getInstance(NettyHttpStepNavigator.class);

    @Test
    public void extractPath_simple() {
        assertThat(handler.extractPath("").toString()).isEqualTo("");
        assertThat(handler.extractPath("foo").toString()).isEqualTo("foo");
        assertThat(handler.extractPath("foo-bar").toString()).isEqualTo("foo-bar");

        assertThat(handler.extractPath("/").toString()).isEqualTo("/");
        assertThat(handler.extractPath("///").toString()).isEqualTo("///");

        assertThat(handler.extractPath("/foo").toString()).isEqualTo("/foo");
        assertThat(handler.extractPath("/foo/bar").toString()).isEqualTo("/foo/bar");
        assertThat(handler.extractPath("/foo/bar/").toString()).isEqualTo("/foo/bar/");

        assertThat(handler.extractPath("?").toString()).isEqualTo("");
        assertThat(handler.extractPath("#").toString()).isEqualTo("");
        assertThat(handler.extractPath("/?").toString()).isEqualTo("/");
        assertThat(handler.extractPath("/?key=value").toString()).isEqualTo("/");

        assertThat(handler.extractPath("/foo?").toString()).isEqualTo("/foo");
        assertThat(handler.extractPath("/foo?key=value").toString()).isEqualTo("/foo");
        assertThat(handler.extractPath("/foo?key=value&key=").toString()).isEqualTo("/foo");
        assertThat(handler.extractPath("/foo?&=").toString()).isEqualTo("/foo");

        assertThat(handler.extractPath("/#").toString()).isEqualTo("/");
        assertThat(handler.extractPath("/#ignore").toString()).isEqualTo("/");
        assertThat(handler.extractPath("/?#").toString()).isEqualTo("/");
        assertThat(handler.extractPath("/?key=value#").toString()).isEqualTo("/");
    }

    @Test
    public void extractPath_trailingSlash() {
        AppSettings appSettings = injector.getInstance(AppSettings.class);
        appSettings.setProperty("netty.url.trailing.slash.ignore", true);

        assertThat(handler.extractPath("a").toString()).isEqualTo("a");
        assertThat(handler.extractPath("foo").toString()).isEqualTo("foo");

        assertThat(handler.extractPath("/").toString()).isEqualTo("/");
        assertThat(handler.extractPath("//").toString()).isEqualTo("/");
        assertThat(handler.extractPath("/foo").toString()).isEqualTo("/foo");
        assertThat(handler.extractPath("/foo/").toString()).isEqualTo("/foo");
        assertThat(handler.extractPath("/foo//").toString()).isEqualTo("/foo/");

        assertThat(handler.extractPath("/foo/bar").toString()).isEqualTo("/foo/bar");
        assertThat(handler.extractPath("/foo/bar/").toString()).isEqualTo("/foo/bar");
    }
}
