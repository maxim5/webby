package io.webby.testing;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class AssertBasics {
    @SuppressWarnings("unchecked")
    public static <T> void assertOneOf(@Nullable T value, @Nullable T @NotNull ... expected) {
        assertThat(value).isIn(Arrays.asList(expected));
    }

    public static <T> void assertOneOf(@Nullable T value, @NotNull Iterable<T> expected) {
        assertThat(value).isIn(expected);
    }

    public static <K, V> @NotNull Map<K, V> asMap(@Nullable Object @NotNull ... items) {
        return asMap(List.of(items));
    }

    @SuppressWarnings("unchecked")
    public static <K, V> @NotNull Map<K, V> asMap(@NotNull List<?> items) {
        assertEquals(0, items.size() % 2);
        LinkedHashMap<K, V> result = new LinkedHashMap<>();
        for (int i = 0; i < items.size(); i += 2) {
            result.put((K) items.get(i), (V) items.get(i + 1));
        }
        return result;
    }

}
