package io.webby.netty.response;

import io.webby.testing.Testing;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static io.webby.testing.AssertBasics.assertOneOf;
import static io.webby.testing.AssertResponse.ICON_MIME_TYPES;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class StaticServingTest {
    private final StaticServing serving = Testing.testStartup().getInstance(StaticServing.class);

    @Test
    public void guessContentType_simple() {
        assertEquals("image/jpeg", serving.guessContentType(Path.of("foo.jpg")));
        assertEquals("image/png", serving.guessContentType(Path.of("foo.png")));
        assertEquals("image/gif", serving.guessContentType(Path.of("foo.gif")));

        assertEquals("text/css", serving.guessContentType(Path.of("foo.css")));
        assertEquals("application/javascript", serving.guessContentType(Path.of("foo.js")));
        assertEquals("application/xml", serving.guessContentType(Path.of("foo.xml")));

        assertOneOf(serving.guessContentType(Path.of("foo.ico")), ICON_MIME_TYPES);
    }
}
