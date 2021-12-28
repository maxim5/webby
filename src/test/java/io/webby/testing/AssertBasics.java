package io.webby.testing;

import io.webby.util.collect.EasyMaps;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class AssertBasics {
    @SuppressWarnings("unchecked")
    public static <T> void assertOneOf(@Nullable T value, @Nullable T @NotNull ... expected) {
        assertThat(value).isIn(Arrays.asList(expected));
    }

    public static <T> void assertOneOf(@Nullable T value, @NotNull Iterable<T> expected) {
        assertThat(value).isIn(expected);
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static <T> void assertEmpty(@Nullable Optional<T> optional) {
        assertNotNull(optional);
        assertTrue(optional.isEmpty());
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static <T> void assertPresent(@Nullable Optional<T> optional, @NotNull T expected) {
        assertNotNull(optional);
        assertTrue(optional.isPresent());
        assertEquals(expected, optional.get());
    }

    public static void assertMapContents(@NotNull Map<?, ?> map, @Nullable Object @NotNull ... expected) {
        LinkedHashMap<Object, Object> expectedMap = EasyMaps.asMap(expected);
        Set<Object> keys = Stream.concat(map.keySet().stream(), expectedMap.keySet().stream()).collect(Collectors.toSet());
        for (Object key : keys) {
            assertEquals(expectedMap.get(key), map.get(key));
        }
    }
}
