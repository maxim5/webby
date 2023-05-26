package io.webby.url.impl;

import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;

public class UrlFixTest {
    @Test
    public void join_with_slash() {
        assertThat(UrlFix.joinWithSlash()).isEqualTo("");
        assertThat(UrlFix.joinWithSlash("")).isEqualTo("");

        assertThat(UrlFix.joinWithSlash("foo")).isEqualTo("foo");
        assertThat(UrlFix.joinWithSlash("foo/")).isEqualTo("foo/");
        assertThat(UrlFix.joinWithSlash("/foo/")).isEqualTo("/foo/");

        assertThat(UrlFix.joinWithSlash("foo", "")).isEqualTo("foo");
        assertThat(UrlFix.joinWithSlash("", "foo")).isEqualTo("foo");
        assertThat(UrlFix.joinWithSlash("", "foo", "", "")).isEqualTo("foo");

        assertThat(UrlFix.joinWithSlash("foo", "bar")).isEqualTo("foo/bar");
        assertThat(UrlFix.joinWithSlash("foo/", "bar")).isEqualTo("foo/bar");
        assertThat(UrlFix.joinWithSlash("foo/", "/bar")).isEqualTo("foo/bar");
        assertThat(UrlFix.joinWithSlash("foo", "/bar")).isEqualTo("foo/bar");
        assertThat(UrlFix.joinWithSlash("foo//", "///bar")).isEqualTo("foo/bar");

        assertThat(UrlFix.joinWithSlash("/foo", "bar")).isEqualTo("/foo/bar");
        assertThat(UrlFix.joinWithSlash("/foo", "/bar")).isEqualTo("/foo/bar");
        assertThat(UrlFix.joinWithSlash("/foo/", "/bar")).isEqualTo("/foo/bar");

        assertThat(UrlFix.joinWithSlash("/", "foo", "bar")).isEqualTo("/foo/bar");
        assertThat(UrlFix.joinWithSlash("/", "/foo/", "/bar")).isEqualTo("/foo/bar");
        assertThat(UrlFix.joinWithSlash("/", "/foo/", "/bar", "")).isEqualTo("/foo/bar");
    }
}
