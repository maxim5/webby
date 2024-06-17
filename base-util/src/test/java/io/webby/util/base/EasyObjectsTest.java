package io.webby.util.base;

import org.junit.jupiter.api.Test;

import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

@SuppressWarnings({ "ObviousNullCheck", "ConstantValue" })
public class EasyObjectsTest {
    private static final String NULL = null;

    @Test
    public void firstNonNull_of_two_obj() {
        assertThat(EasyObjects.firstNonNull("foo", "bar")).isEqualTo("foo");
        assertThat(EasyObjects.firstNonNull("foo", NULL)).isEqualTo("foo");
        assertThat(EasyObjects.firstNonNull(NULL, "bar")).isEqualTo("bar");
        assertThrows(NullPointerException.class, () -> EasyObjects.firstNonNull(NULL, NULL));
    }

    @Test
    public void firstNonNull_of_obj_and_supplier() {
        assertThat(EasyObjects.firstNonNull("foo", () -> "bar")).isEqualTo("foo");
        assertThat(EasyObjects.firstNonNull("foo", () -> NULL)).isEqualTo("foo");
        assertThat(EasyObjects.firstNonNull(NULL, () -> "bar")).isEqualTo("bar");
        assertThrows(NullPointerException.class, () -> EasyObjects.firstNonNull(NULL, () -> NULL));
        assertThat(EasyObjects.firstNonNull("foo", () -> fail("Must not be called"))).isEqualTo("foo");
    }

    @Test
    public void firstNonNull_of_two_suppliers() {
        assertThat(EasyObjects.<String>firstNonNull(() -> "foo", () -> "bar")).isEqualTo("foo");
        assertThat(EasyObjects.<String>firstNonNull(() -> "foo", () -> NULL)).isEqualTo("foo");
        assertThat(EasyObjects.<String>firstNonNull(() -> NULL, () -> "bar")).isEqualTo("bar");
        assertThrows(NullPointerException.class, () -> EasyObjects.<String>firstNonNull(() -> NULL, () -> NULL));
        assertThat(EasyObjects.<String>firstNonNull(() -> "foo", () -> fail("Must not be called"))).isEqualTo("foo");
    }

    @Test
    public void firstNonNull_of_two_suppliers_with_default() {
        assertThat(EasyObjects.firstNonNull(() -> "foo", () -> "bar", "def")).isEqualTo("foo");
        assertThat(EasyObjects.firstNonNull(() -> "foo", () -> NULL, "def")).isEqualTo("foo");
        assertThat(EasyObjects.firstNonNull(() -> NULL, () -> "bar", "def")).isEqualTo("bar");
        assertThat(EasyObjects.firstNonNull(() -> NULL, () -> NULL, "def")).isEqualTo("def");
        assertThat(EasyObjects.firstNonNull(() -> "foo", () -> fail("Must not be called"), "def")).isEqualTo("foo");
    }

    @Test
    public void firstNonNull_of_iterable() {
        assertThat(EasyObjects.<String>firstNonNull(List.of(() -> "foo"))).isEqualTo("foo");
        assertThat(EasyObjects.<String>firstNonNull(List.of(() -> "foo", () -> "bar"))).isEqualTo("foo");
        assertThat(EasyObjects.<String>firstNonNull(List.of(() -> NULL, () -> "bar"))).isEqualTo("bar");
        assertThrows(NullPointerException.class, () -> EasyObjects.<String>firstNonNull(List.of(() -> NULL, () -> NULL)));
        assertThat(EasyObjects.<String>firstNonNull(List.of(() -> "foo", () -> fail("Must not be called")))).isEqualTo("foo");
    }

    @Test
    public void firstNonNull_of_iterable_with_default() {
        assertThat(EasyObjects.firstNonNull(List.of(() -> "foo"), "def")).isEqualTo("foo");
        assertThat(EasyObjects.firstNonNull(List.of(() -> NULL), "def")).isEqualTo("def");
        assertThat(EasyObjects.firstNonNull(List.of(() -> "foo", () -> "bar"), "def")).isEqualTo("foo");
        assertThat(EasyObjects.firstNonNull(List.of(() -> NULL, () -> "bar"), "def")).isEqualTo("bar");
        assertThat(EasyObjects.firstNonNull(List.of(() -> NULL, () -> NULL), "def")).isEqualTo("def");
        assertThat(EasyObjects.firstNonNull(List.of(() -> "foo", () -> fail("Must not be called")), "def")).isEqualTo("foo");
    }

    @Test
    public void firstNonNullIfExist_of_two_obj() {
        assertThat(EasyObjects.firstNonNullIfExist("foo", "bar")).isEqualTo("foo");
        assertThat(EasyObjects.firstNonNullIfExist("foo", NULL)).isEqualTo("foo");
        assertThat(EasyObjects.firstNonNullIfExist(NULL, "bar")).isEqualTo("bar");
        assertThat(EasyObjects.firstNonNullIfExist(NULL, NULL)).isEqualTo(NULL);
    }

    @Test
    public void firstNonNullIfExist_of_obj_and_supplier() {
        assertThat(EasyObjects.firstNonNullIfExist("foo", () -> "bar")).isEqualTo("foo");
        assertThat(EasyObjects.firstNonNullIfExist("foo", () -> NULL)).isEqualTo("foo");
        assertThat(EasyObjects.firstNonNullIfExist(NULL, () -> "bar")).isEqualTo("bar");
        assertThat(EasyObjects.firstNonNullIfExist(NULL, () -> NULL)).isEqualTo(NULL);
        assertThat(EasyObjects.firstNonNullIfExist("foo", () -> fail("Must not be called"))).isEqualTo("foo");
    }

    @Test
    public void firstNonNullIfExist_of_two_suppliers() {
        assertThat(EasyObjects.<String>firstNonNullIfExist(() -> "foo", () -> "bar")).isEqualTo("foo");
        assertThat(EasyObjects.<String>firstNonNullIfExist(() -> "foo", () -> NULL)).isEqualTo("foo");
        assertThat(EasyObjects.<String>firstNonNullIfExist(() -> NULL, () -> "bar")).isEqualTo("bar");
        assertThat(EasyObjects.<String>firstNonNullIfExist(() -> NULL, () -> NULL)).isEqualTo(NULL);
        assertThat(EasyObjects.<String>firstNonNullIfExist(() -> "foo", () -> fail("Must not be called"))).isEqualTo("foo");
    }

    @Test
    public void firstNonNullIfExist_of_two_suppliers_with_default() {
        assertThat(EasyObjects.firstNonNullIfExist(() -> "foo", () -> "bar", "def")).isEqualTo("foo");
        assertThat(EasyObjects.firstNonNullIfExist(() -> "foo", () -> "bar", NULL)).isEqualTo("foo");
        assertThat(EasyObjects.firstNonNullIfExist(() -> "foo", () -> NULL, "def")).isEqualTo("foo");
        assertThat(EasyObjects.firstNonNullIfExist(() -> NULL, () -> "bar", "def")).isEqualTo("bar");
        assertThat(EasyObjects.firstNonNullIfExist(() -> NULL, () -> NULL, "def")).isEqualTo("def");
        assertThat(EasyObjects.firstNonNullIfExist(() -> NULL, () -> NULL, NULL)).isEqualTo(NULL);
        assertThat(EasyObjects.firstNonNullIfExist(() -> "foo", () -> fail("Must not be called"), NULL)).isEqualTo("foo");
    }

    @Test
    public void firstNonNullIfExist_of_iterable() {
        assertThat(EasyObjects.<String>firstNonNullIfExist(List.of(() -> "foo"))).isEqualTo("foo");
        assertThat(EasyObjects.<String>firstNonNullIfExist(List.of(() -> "foo", () -> "bar"))).isEqualTo("foo");
        assertThat(EasyObjects.<String>firstNonNullIfExist(List.of(() -> NULL, () -> "bar"))).isEqualTo("bar");
        assertThat(EasyObjects.<String>firstNonNullIfExist(List.of(() -> NULL, () -> NULL))).isEqualTo(NULL);
        assertThat(EasyObjects.<String>firstNonNullIfExist(List.of(() -> "foo", () -> fail("Must not be called")))).isEqualTo("foo");
    }

    @Test
    public void firstNonNullIfExist_of_iterable_with_default() {
        assertThat(EasyObjects.firstNonNullIfExist(List.of(() -> "foo"), "def")).isEqualTo("foo");
        assertThat(EasyObjects.firstNonNullIfExist(List.of(() -> NULL), "def")).isEqualTo("def");
        assertThat(EasyObjects.firstNonNullIfExist(List.of(() -> "foo"), NULL)).isEqualTo("foo");
        assertThat(EasyObjects.firstNonNullIfExist(List.of(() -> NULL), NULL)).isEqualTo(NULL);

        assertThat(EasyObjects.firstNonNullIfExist(List.of(() -> "foo", () -> "bar"), "def")).isEqualTo("foo");
        assertThat(EasyObjects.firstNonNullIfExist(List.of(() -> NULL, () -> "bar"), "def")).isEqualTo("bar");
        assertThat(EasyObjects.firstNonNullIfExist(List.of(() -> NULL, () -> NULL), "def")).isEqualTo("def");
        assertThat(EasyObjects.firstNonNullIfExist(List.of(() -> NULL, () -> NULL), NULL)).isEqualTo(NULL);
        assertThat(EasyObjects.firstNonNullIfExist(List.of(() -> "foo", () -> fail("Must not be called")), NULL)).isEqualTo("foo");
    }
}
