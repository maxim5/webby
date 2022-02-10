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
        assertEquals(FileExt.EMPTY, FileExt.fromName("", false));
        assertEquals(FileExt.EMPTY, FileExt.fromName("/", false));
        assertEquals(FileExt.EMPTY, FileExt.fromName("///", false));

        assertEquals(FileExt.EMPTY, FileExt.fromName("foo", false));
        assertEquals(FileExt.EMPTY, FileExt.fromName("foo.", false));
        assertEquals(FileExt.EMPTY, FileExt.fromName("foo..", false));

        assertEquals(FileExt.EMPTY, FileExt.fromName(".", false));
        assertEquals(FileExt.EMPTY, FileExt.fromName("..", false));
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
    
    @Test
    public void fromUrl_simple_url() {
        assertEquals(new FileExt(".jpg"), FileExt.fromUrl("www.foo.com/foo.jpg", false));
        assertEquals(new FileExt(".jpg"), FileExt.fromUrl("www.foo.com/foo/bar/baz.jpg", false));
        assertEquals(new FileExt(".jpg"), FileExt.fromUrl("www.foo.com/foo/bar/baz.jpg?a=b&c=d", false));

        assertEquals(new FileExt(".png"), FileExt.fromUrl("http://www.foo.com/foo/bar/baz.png", false));
        assertEquals(new FileExt(".png"), FileExt.fromUrl("https://www.foo.com/foo/bar/baz.png", false));
    }

    @Test
    public void fromUrl_invalid_url() {
        assertEquals(FileExt.EMPTY, FileExt.fromUrl("", false));
        assertEquals(FileExt.EMPTY, FileExt.fromUrl(".", false));
        assertEquals(FileExt.EMPTY, FileExt.fromUrl("/", false));
        assertEquals(FileExt.EMPTY, FileExt.fromUrl("//", false));
        assertEquals(FileExt.EMPTY, FileExt.fromUrl("foo", false));

        assertEquals(new FileExt(".txt"), FileExt.fromUrl(".txt", false));
        assertEquals(new FileExt(".txt"), FileExt.fromUrl("..txt", false));
        assertEquals(new FileExt(".txt"), FileExt.fromUrl("foo.txt", false));
        assertEquals(new FileExt(".txt"), FileExt.fromUrl("/foo.txt", false));
        assertEquals(new FileExt(".txt"), FileExt.fromUrl("/foo/bar.txt", false));
        assertEquals(new FileExt(".txt"), FileExt.fromUrl("/foo.bar.txt", false));
    }

    @Test
    public void fromUrl_empty_path() {
        assertEquals(FileExt.EMPTY, FileExt.fromUrl("http://www.foo.com/", false));
        assertEquals(FileExt.EMPTY, FileExt.fromUrl("www.foo.com/", false));
        assertEquals(new FileExt(".com"), FileExt.fromUrl("www.foo.com", false));
    }
}
