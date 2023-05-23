package io.webby.db.content;

import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;

public class FileExtTest {
    @Test
    public void fromName_simple() {
        assertThat(FileExt.fromName("foo.txt", false)).isEqualTo(new FileExt(".txt"));
        assertThat(FileExt.fromName("foo..txt", false)).isEqualTo(new FileExt(".txt"));
        assertThat(FileExt.fromName("foo.bar.txt", false)).isEqualTo(new FileExt(".txt"));

        assertThat(FileExt.fromName("foo.7z", false)).isEqualTo(new FileExt(".7z"));
        assertThat(FileExt.fromName("foo.o", false)).isEqualTo(new FileExt(".o"));

        assertThat(FileExt.fromName(".txt", false)).isEqualTo(new FileExt(".txt"));
        assertThat(FileExt.fromName(".o", false)).isEqualTo(new FileExt(".o"));
    }

    @Test
    public void fromName_empty() {
        assertThat(FileExt.fromName("", false)).isEqualTo(FileExt.EMPTY);
        assertThat(FileExt.fromName("/", false)).isEqualTo(FileExt.EMPTY);
        assertThat(FileExt.fromName("///", false)).isEqualTo(FileExt.EMPTY);

        assertThat(FileExt.fromName("foo", false)).isEqualTo(FileExt.EMPTY);
        assertThat(FileExt.fromName("foo.", false)).isEqualTo(FileExt.EMPTY);
        assertThat(FileExt.fromName("foo..", false)).isEqualTo(FileExt.EMPTY);

        assertThat(FileExt.fromName(".", false)).isEqualTo(FileExt.EMPTY);
        assertThat(FileExt.fromName("..", false)).isEqualTo(FileExt.EMPTY);
    }

    @Test
    public void fromName_case() {
        assertThat(FileExt.fromName("foo.TXT", false)).isEqualTo(new FileExt(".TXT"));
        assertThat(FileExt.fromName("foo.TXT", true)).isEqualTo(new FileExt(".txt"));
    }

    @Test
    public void fromName_path() {
        assertThat(FileExt.fromName("foo/bar.txt", false)).isEqualTo(new FileExt(".txt"));
    }
    
    @Test
    public void fromUrl_simple_url() {
        assertThat(FileExt.fromUrl("www.foo.com/foo.jpg", false)).isEqualTo(new FileExt(".jpg"));
        assertThat(FileExt.fromUrl("www.foo.com/foo/bar/baz.jpg", false)).isEqualTo(new FileExt(".jpg"));
        assertThat(FileExt.fromUrl("www.foo.com/foo/bar/baz.jpg?a=b&c=d", false)).isEqualTo(new FileExt(".jpg"));

        assertThat(FileExt.fromUrl("http://www.foo.com/foo/bar/baz.png", false)).isEqualTo(new FileExt(".png"));
        assertThat(FileExt.fromUrl("https://www.foo.com/foo/bar/baz.png", false)).isEqualTo(new FileExt(".png"));
    }

    @Test
    public void fromUrl_invalid_url() {
        assertThat(FileExt.fromUrl("", false)).isEqualTo(FileExt.EMPTY);
        assertThat(FileExt.fromUrl(".", false)).isEqualTo(FileExt.EMPTY);
        assertThat(FileExt.fromUrl("/", false)).isEqualTo(FileExt.EMPTY);
        assertThat(FileExt.fromUrl("//", false)).isEqualTo(FileExt.EMPTY);
        assertThat(FileExt.fromUrl("foo", false)).isEqualTo(FileExt.EMPTY);

        assertThat(FileExt.fromUrl(".txt", false)).isEqualTo(new FileExt(".txt"));
        assertThat(FileExt.fromUrl("..txt", false)).isEqualTo(new FileExt(".txt"));
        assertThat(FileExt.fromUrl("foo.txt", false)).isEqualTo(new FileExt(".txt"));
        assertThat(FileExt.fromUrl("/foo.txt", false)).isEqualTo(new FileExt(".txt"));
        assertThat(FileExt.fromUrl("/foo/bar.txt", false)).isEqualTo(new FileExt(".txt"));
        assertThat(FileExt.fromUrl("/foo.bar.txt", false)).isEqualTo(new FileExt(".txt"));
    }

    @Test
    public void fromUrl_empty_path() {
        assertThat(FileExt.fromUrl("http://www.foo.com/", false)).isEqualTo(FileExt.EMPTY);
        assertThat(FileExt.fromUrl("www.foo.com/", false)).isEqualTo(FileExt.EMPTY);
        assertThat(FileExt.fromUrl("www.foo.com", false)).isEqualTo(new FileExt(".com"));
    }
}
