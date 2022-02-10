package io.webby.db.content;

import org.junit.jupiter.api.Test;

import static io.webby.db.content.TestingFiles.*;
import static org.junit.jupiter.api.Assertions.*;

public class FileIdTest {
    @Test
    public void flatFileId_simple() {
        FileId fileId = FileId.flatFileId(contentId("foo"), format("xl"), ext(".txt"));
        assertEquals("foo.xl.txt", fileId.path());
        assertEquals(contentId("foo"), fileId.parseContentIdOrDie());
    }

    @Test
    public void flatFileId_no_extension() {
        FileId fileId = FileId.flatFileId(contentId("a"), format("b"), FileExt.EMPTY);
        assertEquals("a.b", fileId.path());
        assertEquals(contentId("a"), fileId.parseContentIdOrDie());
    }

    @Test
    public void nestedFileId_simple() {
        FileId fileId = FileId.nestedFileId("foo/bar", contentId("baz"), format("xl"), ext(".txt"));
        assertEquals("foo/bar/baz.xl.txt", fileId.path());
        assertEquals(contentId("baz"), fileId.parseContentIdOrDie());
    }

    @Test
    public void nestedFileId_no_extension() {
        FileId fileId = FileId.nestedFileId("foo", contentId("a"), format("b"), FileExt.EMPTY);
        assertEquals("foo/a.b", fileId.path());
        assertEquals(contentId("a"), fileId.parseContentIdOrDie());
    }

    @Test
    public void isSafe_unsafe_dots() {
        assertFalse(new FileId("..").isSafe());
        assertFalse(new FileId("/..").isSafe());
        assertFalse(new FileId("/../").isSafe());
        assertFalse(new FileId("\\..").isSafe());
        assertFalse(new FileId("\\..\\").isSafe());
        assertFalse(new FileId("/..\\").isSafe());
        assertFalse(new FileId("\\../").isSafe());

        assertFalse(new FileId("../foo/bar").isSafe());
        assertFalse(new FileId("..\\foo\\bar").isSafe());
        assertFalse(new FileId("foo/../bar").isSafe());
        assertFalse(new FileId("foo\\..\\bar").isSafe());
    }

    @Test
    public void isSafe_unsafe_absolute() {
        assertFalse(new FileId("C:/").isSafe());
        assertFalse(new FileId("C:\\").isSafe());
        assertFalse(new FileId("C:/Temp").isSafe());
        assertFalse(new FileId("C:\\Temp").isSafe());

        assertFalse(new FileId("/").isSafe());
        assertFalse(new FileId("/home").isSafe());
        assertFalse(new FileId("/home/root").isSafe());
        assertFalse(new FileId("~root").isSafe());
        assertFalse(new FileId("~/root").isSafe());
    }

    @Test
    public void isSafe_safe() {
        assertTrue(new FileId("foo").isSafe());
        assertTrue(new FileId("foo.").isSafe());
        assertTrue(new FileId("foo..").isSafe());

        assertTrue(new FileId("foo/bar").isSafe());
        assertTrue(new FileId("foo/bar.txt").isSafe());
        assertTrue(new FileId("foo/bar..txt").isSafe());
    }
}
