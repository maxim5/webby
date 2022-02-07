package io.webby.util.collect;

import io.webby.util.reflect.EasyMembers;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ArrayTest {
    @Test
    public void array_of_empty() throws Exception {
        Array<String> array = Array.of();
        assertArray(array);
        assertUnderlyingArrayType(array, String[].class);
    }

    @Test
    public void array_of_not_null_items() throws Exception {
        Array<String> array = Array.of("1", "2");
        assertArray(array, "1", "2");
        assertUnderlyingArrayType(array, String[].class);
    }

    @Test
    public void array_of_duplicate_items() throws Exception {
        Array<String> array = Array.of("1", "1");
        assertArray(array, "1", "1");
        assertUnderlyingArrayType(array, String[].class);
    }

    @Test
    public void array_of_nullable_items() throws Exception {
        Array<String> array = Array.of("1", null, "2");
        assertArray(array, "1", null, "2");
        assertUnderlyingArrayType(array, String[].class);
    }

    @Test
    public void array_of_all_nullable_items() throws Exception {
        Array<String> array = Array.of(null, null);
        assertArray(array, null, null);
        assertUnderlyingArrayType(array, String[].class);
    }

    @Test
    public void array_of_ints() throws Exception {
        Array<Integer> array = Array.of(1, 2, 3);
        assertArray(array, 1, 2, 3);
        assertUnderlyingArrayType(array, Integer[].class);
    }

    @Test
    public void array_of_mixed_types() throws Exception {
        Array<Object> array = Array.of(1, "2");
        assertArray(array, 1, "2");
        assertUnderlyingArrayType(array, Object[].class);
    }

    @Test
    public void array_builder_of_add() throws Exception {
        Array<String> array = Array.Builder.of("1", "2").add("3").toArray();
        assertArray(array, "1", "2", "3");
        assertUnderlyingArrayType(array, String[].class);
    }

    @Test
    public void array_builder_of_add_all_vararg() throws Exception {
        Array<String> array = Array.Builder.of("1", "2").addAll("3").toArray();
        assertArray(array, "1", "2", "3");
        assertUnderlyingArrayType(array, String[].class);
    }

    @Test
    public void array_builder_of_add_all_list() throws Exception {
        Array<String> array = Array.Builder.of("1", "2").addAll(List.of("3", "4")).toArray();
        assertArray(array, "1", "2", "3", "4");
        assertUnderlyingArrayType(array, String[].class);
    }

    @Test
    public void array_builder_start_empty() throws Exception {
        Array<String> array = Array.Builder.<String>of().add("3").addAll("4").toArray();
        assertArray(array, "3", "4");
        assertUnderlyingArrayType(array, String[].class);
    }

    @Test
    public void array_to_builder() throws Exception {
        Array<String> array = Array.of("1", "2").toBuilder().add("3").addAll("4", "5").toArray();
        assertArray(array, "1", "2", "3", "4", "5");
        assertUnderlyingArrayType(array, String[].class);
    }

    @SafeVarargs
    private static <T> void assertArray(@NotNull Array<T> array, @Nullable T @NotNull ... expected) {
        assertThat(array).hasSize(expected.length);
        assertThat(array).containsExactlyElementsIn(expected).inOrder();
        List<T> expectedList = Arrays.asList(expected);
        for (int i = 0; i < expected.length; i++) {
            T item = expected[i];
            assertEquals(item, array.get(i));
            assertEquals(expectedList.indexOf(item), array.indexOf(item));
        }
    }

    private static void assertUnderlyingArrayType(@NotNull Array<?> array,
                                                  @NotNull Class<?> klass) throws IllegalAccessException {
        Field field = EasyMembers.findField(BaseArray.class, "arr");
        assertNotNull(field);
        field.setAccessible(true);
        assertEquals(klass, field.get(array).getClass());
    }
}
