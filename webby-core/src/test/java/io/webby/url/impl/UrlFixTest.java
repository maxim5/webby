package io.webby.url.impl;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UrlFixTest {
    @Test
    public void join_with_slash() {
        assertEquals("", UrlFix.joinWithSlash());
        assertEquals("", UrlFix.joinWithSlash(""));

        assertEquals("foo", UrlFix.joinWithSlash("foo"));
        assertEquals("foo/", UrlFix.joinWithSlash("foo/"));
        assertEquals("/foo/", UrlFix.joinWithSlash("/foo/"));

        assertEquals("foo", UrlFix.joinWithSlash("foo", ""));
        assertEquals("foo", UrlFix.joinWithSlash("", "foo"));
        assertEquals("foo", UrlFix.joinWithSlash("", "foo", "", ""));

        assertEquals("foo/bar", UrlFix.joinWithSlash("foo", "bar"));
        assertEquals("foo/bar", UrlFix.joinWithSlash("foo/", "bar"));
        assertEquals("foo/bar", UrlFix.joinWithSlash("foo/", "/bar"));
        assertEquals("foo/bar", UrlFix.joinWithSlash("foo", "/bar"));
        assertEquals("foo/bar", UrlFix.joinWithSlash("foo//", "///bar"));

        assertEquals("/foo/bar", UrlFix.joinWithSlash("/foo", "bar"));
        assertEquals("/foo/bar", UrlFix.joinWithSlash("/foo", "/bar"));
        assertEquals("/foo/bar", UrlFix.joinWithSlash("/foo/", "/bar"));

        assertEquals("/foo/bar", UrlFix.joinWithSlash("/", "foo", "bar"));
        assertEquals("/foo/bar", UrlFix.joinWithSlash("/", "/foo/", "/bar"));
        assertEquals("/foo/bar", UrlFix.joinWithSlash("/", "/foo/", "/bar", ""));
    }
}
