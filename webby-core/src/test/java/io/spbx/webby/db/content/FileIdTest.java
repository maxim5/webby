package io.spbx.webby.db.content;

import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static io.spbx.webby.db.content.TestingFiles.*;

public class FileIdTest {
    @Test
    public void flatFileId_simple() {
        FileId fileId = FileId.flatFileId(contentId("foo"), format("xl"), ext(".txt"));
        assertThat(fileId.path()).isEqualTo("foo.xl.txt");
        assertThat(fileId.parseContentIdOrDie()).isEqualTo(contentId("foo"));
    }

    @Test
    public void flatFileId_no_extension() {
        FileId fileId = FileId.flatFileId(contentId("a"), format("b"), FileExt.EMPTY);
        assertThat(fileId.path()).isEqualTo("a.b");
        assertThat(fileId.parseContentIdOrDie()).isEqualTo(contentId("a"));
    }

    @Test
    public void nestedFileId_simple() {
        FileId fileId = FileId.nestedFileId("foo/bar", contentId("baz"), format("xl"), ext(".txt"));
        assertThat(fileId.path()).isEqualTo("foo/bar/baz.xl.txt");
        assertThat(fileId.parseContentIdOrDie()).isEqualTo(contentId("baz"));
    }

    @Test
    public void nestedFileId_no_extension() {
        FileId fileId = FileId.nestedFileId("foo", contentId("a"), format("b"), FileExt.EMPTY);
        assertThat(fileId.path()).isEqualTo("foo/a.b");
        assertThat(fileId.parseContentIdOrDie()).isEqualTo(contentId("a"));
    }

    @Test
    public void isSafe_unsafe_dots() {
        assertThat(new FileId("..").isSafe()).isFalse();
        assertThat(new FileId("/..").isSafe()).isFalse();
        assertThat(new FileId("/../").isSafe()).isFalse();
        assertThat(new FileId("\\..").isSafe()).isFalse();
        assertThat(new FileId("\\..\\").isSafe()).isFalse();
        assertThat(new FileId("/..\\").isSafe()).isFalse();
        assertThat(new FileId("\\../").isSafe()).isFalse();

        assertThat(new FileId("../foo/bar").isSafe()).isFalse();
        assertThat(new FileId("..\\foo\\bar").isSafe()).isFalse();
        assertThat(new FileId("foo/../bar").isSafe()).isFalse();
        assertThat(new FileId("foo\\..\\bar").isSafe()).isFalse();
    }

    @Test
    public void isSafe_unsafe_absolute() {
        assertThat(new FileId("C:/").isSafe()).isFalse();
        assertThat(new FileId("C:\\").isSafe()).isFalse();
        assertThat(new FileId("C:/Temp").isSafe()).isFalse();
        assertThat(new FileId("C:\\Temp").isSafe()).isFalse();

        assertThat(new FileId("/").isSafe()).isFalse();
        assertThat(new FileId("/home").isSafe()).isFalse();
        assertThat(new FileId("/home/root").isSafe()).isFalse();
        assertThat(new FileId("~root").isSafe()).isFalse();
        assertThat(new FileId("~/root").isSafe()).isFalse();
    }

    @Test
    public void isSafe_safe() {
        assertThat(new FileId("foo").isSafe()).isTrue();
        assertThat(new FileId("foo.").isSafe()).isTrue();
        assertThat(new FileId("foo..").isSafe()).isTrue();

        assertThat(new FileId("foo/bar").isSafe()).isTrue();
        assertThat(new FileId("foo/bar.txt").isSafe()).isTrue();
        assertThat(new FileId("foo/bar..txt").isSafe()).isTrue();
    }
}
