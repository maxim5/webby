package io.webby.db.content;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FileIdTest {
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
