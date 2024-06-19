package io.spbx.util.lazy;

import com.google.errorprone.annotations.ThreadSafe;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * An implementation of {@link CacheCompute} using an {@link AtomicReference}.
 */
@ThreadSafe
public class AtomicCacheCompute<T> implements CacheCompute<T> {
    protected final AtomicReference<T> ref = new AtomicReference<>(null);

    public static <T> @NotNull CacheCompute<T> createEmpty() {
        return new AtomicCacheCompute<>();
    }

    @Override
    public @NotNull T getOrCompute(@NotNull Supplier<T> supplier) {
        return setIfAbsent(ref, supplier);
    }

    private static <T> @NotNull T setIfAbsent(@NotNull AtomicReference<T> reference, @NotNull Supplier<T> supplier) {
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
