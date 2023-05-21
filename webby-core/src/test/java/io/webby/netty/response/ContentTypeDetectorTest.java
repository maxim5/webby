package io.webby.netty.response;

import io.webby.testing.Testing;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static io.webby.testing.AssertBasics.assertOneOf;
import static io.webby.testing.AssertResponse.ICON_MIME_TYPES;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ContentTypeDetectorTest {
    private final ContentTypeDetector detector = Testing.testStartup().getInstance(ContentTypeDetector.class);

    @Test
    public void guessContentType_simple() {
        assertEquals("image/jpeg", detector.guessContentType(Path.of("foo.jpg")));
        assertEquals("image/png", detector.guessContentType(Path.of("foo.png")));
        assertEquals("image/gif", detector.guessContentType(Path.of("foo.gif")));

        assertEquals("text/css", detector.guessContentType(Path.of("foo.css")));
        assertEquals("application/xml", detector.guessContentType(Path.of("foo.xml")));
        assertOneOf(detector.guessContentType(Path.of("foo.js")), "application/javascript", "text/javascript");

        assertOneOf(detector.guessContentType(Path.of("foo.ico")), ICON_MIME_TYPES);
    }

    @Test
    public void guessContentType_audio() {
        assertEquals("audio/mpeg", detector.guessContentType(Path.of("foo.mp3")));
        assertEquals("audio/mpeg", detector.guessContentType(Path.of("foo.m3a")));
    }

    @Test
    public void guessContentType_video() {
        assertEquals("video/mpeg", detector.guessContentType(Path.of("foo.mpg")));
        assertEquals("video/mpeg", detector.guessContentType(Path.of("foo.mpeg")));
        assertEquals("video/webm", detector.guessContentType(Path.of("foo.webm")));
        assertEquals("video/x-flv", detector.guessContentType(Path.of("foo.flv")));
    }

    @Test
    public void guessContentType_exclusive_simplemagic() {
        assertEquals("audio/midi", detector.guessContentType(Path.of("foo.rmi")));
        assertEquals("image/vnd.adobe.photoshop", detector.guessContentType(Path.of("foo.psd")));
    }

    @Test
    public void guessContentType_exclusive_mimeutil() {
        assertEquals("application/x-iphone", detector.guessContentType(Path.of("foo.iii")));
        assertEquals("application/x-cocoa", detector.guessContentType(Path.of("foo.cco")));
        assertEquals("application/x-httpd-imap", detector.guessContentType(Path.of("foo.imap")));
    }
}
