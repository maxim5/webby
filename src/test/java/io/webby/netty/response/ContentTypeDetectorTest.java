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
        assertEquals("application/javascript", detector.guessContentType(Path.of("foo.js")));
        assertEquals("application/xml", detector.guessContentType(Path.of("foo.xml")));

        assertOneOf(detector.guessContentType(Path.of("foo.ico")), ICON_MIME_TYPES);
    }
}
