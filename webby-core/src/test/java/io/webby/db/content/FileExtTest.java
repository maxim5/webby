package io.webby.db.content;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FileExtTest {
    @Test
    public void fromName_simple() {
        assertEquals(new FileExt(".txt"), FileExt.fromName("foo.txt", false));
        assertEquals(new FileExt(".txt"), FileExt.fromName("foo..txt", false));
        assertEquals(new FileExt(".txt"), FileExt.fromName("foo.bar.txt", false));

        assertEquals(new FileExt(".7z"), FileExt.fromName("foo.7z", false));
        assertEquals(new FileExt(".o"), FileExt.fromName("foo.o", false));

        assertEquals(new FileExt(".txt"), FileExt.fromName(".txt", false));
        assertEquals(new FileExt(".o"), FileExt.fromName(".o", false));
    }

    @Test
    public void fromName_empty() {
        assertEquals(new FileExt(""), FileExt.fromName("", false));
        assertEquals(new FileExt(""), FileExt.fromName("/", false));
        assertEquals(new FileExt(""), FileExt.fromName("///", false));

        assertEquals(new FileExt(""), FileExt.fromName("foo", false));
        assertEquals(new FileExt(""), FileExt.fromName("foo.", false));
        assertEquals(new FileExt(""), FileExt.fromName("foo..", false));

        assertEquals(new FileExt(""), FileExt.fromName(".", false));
        assertEquals(new FileExt(""), FileExt.fromName("..", false));
    }

    @Test
    public void fromName_case() {
        assertEquals(new FileExt(".TXT"), FileExt.fromName("foo.TXT", false));
        assertEquals(new FileExt(".txt"), FileExt.fromName("foo.TXT", true));
    }

    @Test
    public void fromName_path() {
        assertEquals(new FileExt(".txt"), FileExt.fromName("foo/bar.txt", false));
    }
}
