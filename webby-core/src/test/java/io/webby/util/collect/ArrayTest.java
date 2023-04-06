package io.webby.util.collect;

import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static io.webby.testing.AssertBasics.assertPrivateFieldClass;
import static io.webby.util.base.EasyCast.castAny;
import static org.junit.jupiter.api.Assertions.*;

public class ArrayTest {
    @Test
    public void array_of_empty() {
        Array<String> array = Array.of();
        assertArray(array);
    }

    @Test
    public void array_of_not_null_items() {
        Array<String> array = Array.of("1", "2");
        assertArray(array, "1", "2");
    }

    @Test
    public void array_of_duplicate_items() {
        Array<String> array = Array.of("1", "1");
        assertArray(array, "1", "1");
    }

    @Test
    public void array_of_nullable_items() {
        Array<String> array = Array.of("1", null, "2");
        assertArray(array, "1", null, "2");
    }

    @Test
    public void array_of_all_nullable_items() {
        Array<String> array = Array.of(null, null);
        assertArray(array, null, null);
    }

    @Test
    public void array_of_ints() {
        Array<Integer> array = Array.of(1, 2, 3);
        assertArray(array, 1, 2, 3);
    }

    @Test
    public void array_of_mixed_types() {
        Array<Object> array = Array.of(1, "2");
        assertArray(array, 1, "2");
    }

    @Test
    public void array_builder_of_add() {
        Array<String> array = Array.Builder.of("1", "2").add("3").toArray();
        assertArray(array, "1", "2", "3");
    }

    @Test
    public void array_builder_of_add_all_vararg() {
        Array<String> array = Array.Builder.of("1", "2").addAll("3").toArray();
        assertArray(array, "1", "2", "3");
    }

    @Test
    public void array_builder_of_add_all_list() {
        Array<String> array = Array.Builder.of("1", "2").addAll(List.of("3", "4")).toArray();
        assertArray(array, "1", "2", "3", "4");
    }

    @Test
    public void array_builder_start_empty() {
        Array<String> array = Array.Builder.<String>of().add("3").addAll("4").toArray();
        assertArray(array, "3", "4");
    }

    @Test
    public void array_to_builder() {
        Array<String> array = Array.of("1", "2").toBuilder().add("3").addAll("4", "5").toArray();
        assertArray(array, "1", "2", "3", "4", "5");
    }

    @Test
    public void array_set() {
        Array<Integer> array = Array.of(1, 2, 3);
        assertEquals(array.set(1, 7), 2);
        assertArray(array, 1, 7, 3);
    }

    @Test
    public void array_add_throws() {
        assertThrows(UnsupportedOperationException.class, () -> Array.of().add(4));
        assertThrows(UnsupportedOperationException.class, () -> Array.of(1, 2, 3).add(4));
        assertThrows(UnsupportedOperationException.class, () -> Array.of(1, 2, 3).add(1, 4));
        assertThrows(UnsupportedOperationException.class, () -> Array.of(1, 2, 3).addAll(List.of(4, 5)));
    }

    @Test
    public void array_remove_throws() {
        assertThrows(UnsupportedOperationException.class, () -> Array.of("1").remove("1"));
        assertThrows(UnsupportedOperationException.class, () -> Array.of("1").remove(0));
        assertThrows(UnsupportedOperationException.class, () -> Array.of("1").removeIf("1"::equals));
        assertThrows(UnsupportedOperationException.class, () -> Array.of("1").removeAll(List.of("1", "2")));
        assertThrows(UnsupportedOperationException.class, () -> Array.of("1").clear());
    }

    @Test
    public void array_replaceAll() {
        Array<Integer> array = Array.of(1, 2, 3);
        array.replaceAll(x -> x * 2);
        assertArray(array, 2, 4, 6);
    }

    @Test
    public void array_sort() {
        Array<Integer> array = Array.of(2, 1, 3);
        array.sort(Integer::compare);
        assertArray(array, 1, 2, 3);
    }

    @Test
    public void array_underlying_type() {
        assertUnderlyingArrayType(Array.<String>of(), String[].class);
        assertUnderlyingArrayType(Array.of("1", "2"), String[].class);
        assertUnderlyingArrayType(Array.of("1", null), String[].class);
        assertUnderlyingArrayType(Array.<String>of(null, null), String[].class);
        assertUnderlyingArrayType(Array.of(1, 2), Integer[].class);
        assertUnderlyingArrayType(Array.<Object>of(1, "2"), Object[].class);
    }

    @Test
    public void array_set_incorrect_type() {
        assertThrows(ArrayStoreException.class, () -> castArray(Array.of(1, 2)).set(0, "x"));
        assertThrows(ArrayStoreException.class, () -> castArray(Array.of("1", "2")).set(0, 1));
    }

    @SafeVarargs
    private static <T> void assertArray(@NotNull Array<T> array, @Nullable T @NotNull ... expected) {
        assertThat(array).hasSize(expected.length);
        assertThat(array).containsExactlyElementsIn(expected).inOrder();
        assertThat(Lists.newArrayList(array.iterator())).containsExactlyElementsIn(expected).inOrder();
        assertThat(Lists.newArrayList(array.listIterator())).containsExactlyElementsIn(expected).inOrder();
        assertThat(array.isEmpty()).isEqualTo(expected.length == 0);

        List<T> expectedList = Arrays.asList(expected);
        for (int i = 0; i < expected.length; i++) {
            T item = expected[i];
            assertEquals(item, array.get(i));
            assertEquals(expectedList.indexOf(item), array.indexOf(item));
            assertEquals(expectedList.lastIndexOf(item), array.lastIndexOf(item));
            assertTrue(array.contains(item));
            assertThat(array.subList(i, i + 1)).containsExactly(item);
        }
        assertThat(array.subList(0, expected.length)).containsExactlyElementsIn(expected).inOrder();
    }

    private static void assertUnderlyingArrayType(@NotNull Array<?> array, @NotNull Class<?> klass) {
        assertPrivateFieldClass(array, "arr", klass);
    }

    private static <T> @NotNull Array<Object> castArray(@NotNull Array<T> array) {
        return castAny(array);
    }
}
