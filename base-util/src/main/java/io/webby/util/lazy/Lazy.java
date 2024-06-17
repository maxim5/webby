package io.webby.util.lazy;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * Allows to store a lazy value. The clients should only provide a way to supply the way.
 * <p>
 * Design note: the API is most convenient for cache/memoize use case. Does not allow nulls.
 */
public class Lazy<T> implements Supplier<T> {
    private final Supplier<T> supplier;
    protected final AtomicReference<T> ref = new AtomicReference<>(null);

    public Lazy(@NotNull Supplier<@NotNull T> supplier) {
        this.supplier = supplier;
    }

    @Override
    public @NotNull T get() {
        T value = ref.get();
        if (value == null) {
            value = supplier.get();
            if (!ref.compareAndSet(null, value)) {
                return ref.get();
            }
        }
        return value;
    }
}
