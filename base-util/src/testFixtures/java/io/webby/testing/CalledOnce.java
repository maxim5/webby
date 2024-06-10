package io.webby.testing;

import io.webby.util.func.ThrowConsumer;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CalledOnce<T, E extends Throwable> implements Consumer<T>, ThrowConsumer<T, E>, AutoCloseable {
    private final AtomicReference<T> value = new AtomicReference<>(null);
    private final AtomicBoolean called = new AtomicBoolean(false);
    private final boolean allowNulls;

    public CalledOnce(boolean allowNulls) {
        this.allowNulls = allowNulls;
    }

    public CalledOnce() {
        this(false);
    }

    @Override
    public void accept(T item) {
        if (!allowNulls) {
            assertNotNull(item, "Called with a null");
        }
        assertTrue(value.compareAndSet(null, item),
                   "Called more than once. First: %s, then: %s".formatted(value.get(), item));
        boolean set = called.compareAndSet(false, true);
        assert set : "Internal error: the flag is already set: " + this;
    }

    public @NotNull T getValue() {
        T result = value.get();
        assertTrue(called.get(), "Expected to be called, but it wasn't");
        if (!allowNulls) {
            assertNotNull(result, "Called with a null");
        }
        return result;
    }

    @Override
    public void close() {
        getValue();
    }

    public @NotNull ThrowConsumer<T, E> alsoCall(@NotNull ThrowConsumer<? super T, E> after) {
        return this.andThen(after);
    }
}
