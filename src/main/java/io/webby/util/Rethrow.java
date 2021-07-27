package io.webby.util;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public interface Rethrow {
    @CanIgnoreReturnValue
    static <T> T rethrow(@NotNull Throwable exception) {
        throw new RuntimeException(exception);
    }

    @CanIgnoreReturnValue
    static <T> T rethrow(@NotNull String message, @NotNull Throwable exception) {
        throw new RuntimeException(message, exception);
    }

    interface Consumers {
        @NotNull
        static <T, E extends Throwable> Consumer<T> rethrow(@NotNull ThrowConsumer<T, E> consumer) {
            return value -> {
                try {
                    consumer.accept(value);
                } catch (Throwable e) {
                    Rethrow.rethrow(e);
                }
            };
        }
    }

    interface Suppliers {
        @NotNull
        static <T, E extends Throwable> Supplier<T> rethrow(@NotNull ThrowSupplier<T, E> supplier) {
            return () -> {
                try {
                    return supplier.get();
                } catch (Throwable e) {
                    return Rethrow.rethrow(e);
                }
            };
        }
    }

    interface Functions {
        @NotNull
        static <T, R, E extends Throwable> Function<T, R> rethrow(@NotNull ThrowFunction<T, R, E> function) {
            return value -> {
                try {
                    return function.apply(value);
                } catch (Throwable e) {
                    return Rethrow.rethrow(e);
                }
            };
        }
    }
}
