package io.webby.testing;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

import static com.google.common.truth.Truth.assertThat;

public class AssertBasics {
    @SuppressWarnings("unchecked")
    public static <T> void assertOneOf(@Nullable T value, @Nullable T @NotNull ... expected) {
        assertThat(value).isIn(Arrays.asList(expected));
    }

    public static <T> void assertOneOf(@Nullable T value, @NotNull Iterable<T> expected) {
        assertThat(value).isIn(expected);
    }
}
