package io.webby.netty.response;

import io.webby.testing.Testing;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static com.google.common.truth.Truth.assertThat;
import static io.webby.testing.AssertResponse.*;

public class ContentTypeDetectorTest {
    private final ContentTypeDetector detector = Testing.testStartup().getInstance(ContentTypeDetector.class);

    @Test
    public void guessContentType_simple() {
        assertThat(detector.guessContentType(Path.of("foo.jpg"))).isEqualTo("image/jpeg");
        assertThat(detector.guessContentType(Path.of("foo.png"))).isEqualTo("image/png");
        assertThat(detector.guessContentType(Path.of("foo.gif"))).isEqualTo("image/gif");

        assertThat(detector.guessContentType(Path.of("foo.css"))).isEqualTo("text/css");
        assertThat(detector.guessContentType(Path.of("foo.xml"))).isEqualTo("application/xml");
        assertThat(detector.guessContentType(Path.of("foo.js"))).isIn(JS_MIME_TYPES);

        assertThat(detector.guessContentType(Path.of("foo.ico"))).isIn(ICON_MIME_TYPES);
    }

    @Test
    public void guessContentType_audio() {
        assertThat(detector.guessContentType(Path.of("foo.mp3"))).isEqualTo("audio/mpeg");
        assertThat(detector.guessContentType(Path.of("foo.m3a"))).isEqualTo("audio/mpeg");
    }

    @Test
    public void guessContentType_video() {
        assertThat(detector.guessContentType(Path.of("foo.mpg"))).isEqualTo("video/mpeg");
        assertThat(detector.guessContentType(Path.of("foo.mpeg"))).isEqualTo("video/mpeg");
        assertThat(detector.guessContentType(Path.of("foo.webm"))).isEqualTo("video/webm");
        assertThat(detector.guessContentType(Path.of("foo.flv"))).isEqualTo("video/x-flv");
    }

    @Test
    public void guessContentType_exclusive_simplemagic() {
        assertThat(detector.guessContentType(Path.of("foo.rmi"))).isIn(MIDI_MIME_TYPES);
        assertThat(detector.guessContentType(Path.of("foo.psd"))).isEqualTo("image/vnd.adobe.photoshop");
    }

    @Test
    public void guessContentType_exclusive_mimeutil() {
        assertThat(detector.guessContentType(Path.of("foo.iii"))).isEqualTo("application/x-iphone");
        assertThat(detector.guessContentType(Path.of("foo.cco"))).isEqualTo("application/x-cocoa");
        assertThat(detector.guessContentType(Path.of("foo.imap"))).isEqualTo("application/x-httpd-imap");
    }
}
