package io.webby.netty.response;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static io.webby.netty.response.StaticServing.guessContentType;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class StaticServingTest {
    @Test
    public void guessContentType_simple() throws Exception {
        assertEquals("image/jpeg", guessContentType(Path.of("foo.jpg")));
        assertEquals("image/png", guessContentType(Path.of("foo.png")));
        assertEquals("image/gif", guessContentType(Path.of("foo.gif")));
        assertEquals("image/x-icon", guessContentType(Path.of("foo.ico")));

        assertEquals("text/css", guessContentType(Path.of("foo.css")));
        assertEquals("application/javascript", guessContentType(Path.of("foo.js")));
        assertEquals("application/xml", guessContentType(Path.of("foo.xml")));
    }
}
