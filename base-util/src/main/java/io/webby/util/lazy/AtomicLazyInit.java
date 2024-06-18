package io.webby.util.lazy;

import com.google.errorprone.annotations.ThreadSafe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

/**
 * An implementation of {@link LazyInit} using an {@link AtomicReference}.
 */
@ThreadSafe
public class AtomicLazyInit<T> implements LazyInit<T> {
    protected final AtomicReference<T> ref;

    protected AtomicLazyInit(@Nullable T initValue) {
        ref = new AtomicReference<>(initValue);
    }

    public static <T> @NotNull LazyInit<T> createUninitialized() {
        return new AtomicLazyInit<>(null);
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
    public @NotNull T initializeIfNotYet(@NotNull Supplier<T> valueSupplier) {
        return ref.updateAndGet(current -> current != null ? current : valueSupplier.get());
    }

    @Override
    public @NotNull T getOrDie() {
        return requireNonNull(ref.get());
    }
}
