package io.webby.testing;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicReference;

public class ForcedFailure {
    private final AtomicReference<Throwable> failure;

    public ForcedFailure() {
        this(null);
    }

    public ForcedFailure(@Nullable Throwable throwable) {
        failure = new AtomicReference<>(throwable);
    }

    public boolean isSet() {
        return failure.get() != null;
    }

    @CanIgnoreReturnValue
    public boolean force(@NotNull Throwable throwable) {
        return failure.compareAndSet(null, throwable);
    }

    public void throwIfSet() {
        Throwable throwable = failure.get();
        if (throwable != null) {
            ForcedFailure.throwException(throwable);
        }
    }

    // Idea taken from
    // https://stackoverflow.com/questions/4519557/is-there-a-way-to-throw-an-exception-without-adding-the-throws-declaration
    @SuppressWarnings("unchecked")
    private static <T extends Throwable> void throwException(Throwable exception) throws T {
        throw (T) exception;
    }
}
