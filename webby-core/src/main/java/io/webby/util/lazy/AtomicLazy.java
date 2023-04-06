package io.webby.util.lazy;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

/**
 * A single implementation of both {@link DelayedInitLazy} and {@code DelayedAccessLazy}
 * using an {@link AtomicReference}.
 */
public class AtomicLazy<T> implements DelayedInitLazy<T>, DelayedAccessLazy<T> {
    protected final AtomicReference<T> ref;

    protected AtomicLazy(@Nullable T initValue) {
        ref = new AtomicReference<>(initValue);
    }

    public static <T> @NotNull DelayedAccessLazy<T> emptyLazy() {
        return new AtomicLazy<>(null);
    }

    public static <T> @NotNull DelayedInitLazy<T> ofUninitialized() {
        return new AtomicLazy<>(null);
    }

    @Override
    public boolean isInitialized() {
        return ref.get() != null;
    }

    @Override
    public void initializeOrDie(@NotNull T value) {
        boolean success = ref.compareAndSet(null, value);
        assert success : "Invalid state. %s already initialized: %s".formatted(getClass().getSimpleName(), ref.get());
    }

    @Override
    public void initializeOrCompare(@NotNull T value) {
        boolean success = ref.compareAndSet(null, value);
        assert success || ref.get() == value :
            "Invalid state. %s already initialized with another value: %s. New value: %s"
            .formatted(getClass().getSimpleName(), ref.get(), value);
    }

    @Override
    public @NotNull T lazyGet(@NotNull Supplier<T> supplier) {
        return setIfAbsent(ref, supplier);
    }

    @Override
    public @NotNull T getOrDie() {
        return requireNonNull(ref.get());
    }

    public static <T> @NotNull T setIfAbsent(@NotNull AtomicReference<T> reference, @NotNull Supplier<T> supplier) {
        T value = reference.get();
        if (value == null) {
            value = supplier.get();
            if (!reference.compareAndSet(null, value)) {
                return reference.get();
            }
        }
        return value;
    }
}
