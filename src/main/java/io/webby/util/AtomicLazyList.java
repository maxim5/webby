package io.webby.util;

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AtomicLazyList<T> extends AtomicLazy<ImmutableList<T>> {
    private AtomicLazyList(@Nullable ImmutableList<T> values) {
        super(values);
    }

    public static <T> @NotNull AtomicLazyList<T> ofUninitializedList() {
        return new AtomicLazyList<>(null);
    }

    public static <T> @NotNull AtomicLazyList<T> ofInitialized(@NotNull List<T> values) {
        return new AtomicLazyList<>(ImmutableList.copyOf(values));
    }
}
