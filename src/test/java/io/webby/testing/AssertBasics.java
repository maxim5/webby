package io.webby.testing;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Optional;

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
}
