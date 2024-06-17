package io.webby.util.base;

import org.junit.jupiter.api.Test;

import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

@SuppressWarnings({ "ObviousNullCheck", "ConstantValue" })
public class EasyNullsTest {
    private static final String NULL = null;

    @Test
    public void firstNonNull_of_two_obj() {
        assertThat(EasyNulls.firstNonNull("foo", "bar")).isEqualTo("foo");
        assertThat(EasyNulls.firstNonNull("foo", NULL)).isEqualTo("foo");
        assertThat(EasyNulls.firstNonNull(NULL, "bar")).isEqualTo("bar");
        assertThrows(NullPointerException.class, () -> EasyNulls.firstNonNull(NULL, NULL));
    }

    @Test
    public void firstNonNull_of_obj_and_supplier() {
        assertThat(EasyNulls.firstNonNull("foo", () -> "bar")).isEqualTo("foo");
        assertThat(EasyNulls.firstNonNull("foo", () -> NULL)).isEqualTo("foo");
        assertThat(EasyNulls.firstNonNull(NULL, () -> "bar")).isEqualTo("bar");
        assertThrows(NullPointerException.class, () -> EasyNulls.firstNonNull(NULL, () -> NULL));
        assertThat(EasyNulls.firstNonNull("foo", () -> fail("Must not be called"))).isEqualTo("foo");
    }

    @Test
    public void firstNonNull_of_two_suppliers() {
        assertThat(EasyNulls.<String>firstNonNull(() -> "foo", () -> "bar")).isEqualTo("foo");
        assertThat(EasyNulls.<String>firstNonNull(() -> "foo", () -> NULL)).isEqualTo("foo");
        assertThat(EasyNulls.<String>firstNonNull(() -> NULL, () -> "bar")).isEqualTo("bar");
        assertThrows(NullPointerException.class, () -> EasyNulls.<String>firstNonNull(() -> NULL, () -> NULL));
        assertThat(EasyNulls.<String>firstNonNull(() -> "foo", () -> fail("Must not be called"))).isEqualTo("foo");
    }

    @Test
    public void firstNonNull_of_two_suppliers_with_default() {
        assertThat(EasyNulls.firstNonNull(() -> "foo", () -> "bar", "def")).isEqualTo("foo");
        assertThat(EasyNulls.firstNonNull(() -> "foo", () -> NULL, "def")).isEqualTo("foo");
        assertThat(EasyNulls.firstNonNull(() -> NULL, () -> "bar", "def")).isEqualTo("bar");
        assertThat(EasyNulls.firstNonNull(() -> NULL, () -> NULL, "def")).isEqualTo("def");
        assertThat(EasyNulls.firstNonNull(() -> "foo", () -> fail("Must not be called"), "def")).isEqualTo("foo");
    }

    @Test
    public void firstNonNull_of_iterable() {
        assertThat(EasyNulls.<String>firstNonNull(List.of(() -> "foo"))).isEqualTo("foo");
        assertThat(EasyNulls.<String>firstNonNull(List.of(() -> "foo", () -> "bar"))).isEqualTo("foo");
        assertThat(EasyNulls.<String>firstNonNull(List.of(() -> NULL, () -> "bar"))).isEqualTo("bar");
        assertThrows(NullPointerException.class, () -> EasyNulls.<String>firstNonNull(List.of(() -> NULL, () -> NULL)));
        assertThat(EasyNulls.<String>firstNonNull(List.of(() -> "foo", () -> fail("Must not be called")))).isEqualTo("foo");
    }

    @Test
    public void firstNonNull_of_iterable_with_default() {
        assertThat(EasyNulls.firstNonNull(List.of(() -> "foo"), "def")).isEqualTo("foo");
        assertThat(EasyNulls.firstNonNull(List.of(() -> NULL), "def")).isEqualTo("def");
        assertThat(EasyNulls.firstNonNull(List.of(() -> "foo", () -> "bar"), "def")).isEqualTo("foo");
        assertThat(EasyNulls.firstNonNull(List.of(() -> NULL, () -> "bar"), "def")).isEqualTo("bar");
        assertThat(EasyNulls.firstNonNull(List.of(() -> NULL, () -> NULL), "def")).isEqualTo("def");
        assertThat(EasyNulls.firstNonNull(List.of(() -> "foo", () -> fail("Must not be called")), "def")).isEqualTo("foo");
    }

    @Test
    public void firstNonNullIfExist_of_two_obj() {
        assertThat(EasyNulls.firstNonNullIfExist("foo", "bar")).isEqualTo("foo");
        assertThat(EasyNulls.firstNonNullIfExist("foo", NULL)).isEqualTo("foo");
        assertThat(EasyNulls.firstNonNullIfExist(NULL, "bar")).isEqualTo("bar");
        assertThat(EasyNulls.firstNonNullIfExist(NULL, NULL)).isEqualTo(NULL);
    }

    @Test
    public void firstNonNullIfExist_of_obj_and_supplier() {
        assertThat(EasyNulls.firstNonNullIfExist("foo", () -> "bar")).isEqualTo("foo");
        assertThat(EasyNulls.firstNonNullIfExist("foo", () -> NULL)).isEqualTo("foo");
        assertThat(EasyNulls.firstNonNullIfExist(NULL, () -> "bar")).isEqualTo("bar");
        assertThat(EasyNulls.firstNonNullIfExist(NULL, () -> NULL)).isEqualTo(NULL);
        assertThat(EasyNulls.firstNonNullIfExist("foo", () -> fail("Must not be called"))).isEqualTo("foo");
    }

    @Test
    public void firstNonNullIfExist_of_two_suppliers() {
        assertThat(EasyNulls.<String>firstNonNullIfExist(() -> "foo", () -> "bar")).isEqualTo("foo");
        assertThat(EasyNulls.<String>firstNonNullIfExist(() -> "foo", () -> NULL)).isEqualTo("foo");
        assertThat(EasyNulls.<String>firstNonNullIfExist(() -> NULL, () -> "bar")).isEqualTo("bar");
        assertThat(EasyNulls.<String>firstNonNullIfExist(() -> NULL, () -> NULL)).isEqualTo(NULL);
        assertThat(EasyNulls.<String>firstNonNullIfExist(() -> "foo", () -> fail("Must not be called"))).isEqualTo("foo");
    }

    @Test
    public void firstNonNullIfExist_of_two_suppliers_with_default() {
        assertThat(EasyNulls.firstNonNullIfExist(() -> "foo", () -> "bar", "def")).isEqualTo("foo");
        assertThat(EasyNulls.firstNonNullIfExist(() -> "foo", () -> "bar", NULL)).isEqualTo("foo");
        assertThat(EasyNulls.firstNonNullIfExist(() -> "foo", () -> NULL, "def")).isEqualTo("foo");
        assertThat(EasyNulls.firstNonNullIfExist(() -> NULL, () -> "bar", "def")).isEqualTo("bar");
        assertThat(EasyNulls.firstNonNullIfExist(() -> NULL, () -> NULL, "def")).isEqualTo("def");
        assertThat(EasyNulls.firstNonNullIfExist(() -> NULL, () -> NULL, NULL)).isEqualTo(NULL);
        assertThat(EasyNulls.firstNonNullIfExist(() -> "foo", () -> fail("Must not be called"), NULL)).isEqualTo("foo");
    }

    @Test
    public void firstNonNullIfExist_of_iterable() {
        assertThat(EasyNulls.<String>firstNonNullIfExist(List.of(() -> "foo"))).isEqualTo("foo");
        assertThat(EasyNulls.<String>firstNonNullIfExist(List.of(() -> "foo", () -> "bar"))).isEqualTo("foo");
        assertThat(EasyNulls.<String>firstNonNullIfExist(List.of(() -> NULL, () -> "bar"))).isEqualTo("bar");
        assertThat(EasyNulls.<String>firstNonNullIfExist(List.of(() -> NULL, () -> NULL))).isEqualTo(NULL);
        assertThat(EasyNulls.<String>firstNonNullIfExist(List.of(() -> "foo", () -> fail("Must not be called")))).isEqualTo("foo");
    }

    @Test
    public void firstNonNullIfExist_of_iterable_with_default() {
        assertThat(EasyNulls.firstNonNullIfExist(List.of(() -> "foo"), "def")).isEqualTo("foo");
        assertThat(EasyNulls.firstNonNullIfExist(List.of(() -> NULL), "def")).isEqualTo("def");
        assertThat(EasyNulls.firstNonNullIfExist(List.of(() -> "foo"), NULL)).isEqualTo("foo");
        assertThat(EasyNulls.firstNonNullIfExist(List.of(() -> NULL), NULL)).isEqualTo(NULL);

        assertThat(EasyNulls.firstNonNullIfExist(List.of(() -> "foo", () -> "bar"), "def")).isEqualTo("foo");
        assertThat(EasyNulls.firstNonNullIfExist(List.of(() -> NULL, () -> "bar"), "def")).isEqualTo("bar");
        assertThat(EasyNulls.firstNonNullIfExist(List.of(() -> NULL, () -> NULL), "def")).isEqualTo("def");
        assertThat(EasyNulls.firstNonNullIfExist(List.of(() -> NULL, () -> NULL), NULL)).isEqualTo(NULL);
        assertThat(EasyNulls.firstNonNullIfExist(List.of(() -> "foo", () -> fail("Must not be called")), NULL)).isEqualTo("foo");
    }
}
