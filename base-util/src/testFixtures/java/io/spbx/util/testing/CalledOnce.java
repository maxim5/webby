package io.spbx.util.testing;

import io.spbx.util.base.EasyExceptions.InternalErrors;
import io.spbx.util.func.ThrowConsumer;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static io.spbx.util.testing.MoreTruth.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
        assertThat(value.compareAndSet(null, item))
            .withMessage("Called more than once. First: %s, then: %s", value.get(), item)
            .isTrue();
        boolean set = called.compareAndSet(false, true);
        InternalErrors.assure(set, "Fhe flag is already set: %s", this);
    }

    public @NotNull T getValue() {
        T result = value.get();
        assertThat(called.get()).withMessage("Expected to be called, but it wasn't").isTrue();
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
