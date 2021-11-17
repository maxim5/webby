package io.webby.util.lazy;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

public class AtomicLazy<T> implements DelayedInitLazy<T>, DelayedAccessLazy<T> {
    protected final AtomicReference<T> ref;

    public AtomicLazy(@Nullable T initValue) {
        ref = new AtomicReference<>(initValue);
    }

    public AtomicLazy() {
        this(null);
    }

    public static <T> @NotNull DelayedAccessLazy<T> emptyLazy() {
        return new AtomicLazy<>(null);
    }

    public static <T> @NotNull DelayedInitLazy<T> ofUninitialized() {
        return new AtomicLazy<>(null);
    }

    public static <T> @NotNull DelayedInitLazy<T> ofInitialized(@NotNull T value) {
        return new AtomicLazy<>(value);
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
