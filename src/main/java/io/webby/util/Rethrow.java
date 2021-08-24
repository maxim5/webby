package io.webby.util;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.webby.util.func.ThrowBiConsumer;
import io.webby.util.func.ThrowConsumer;
import io.webby.util.func.ThrowFunction;
import io.webby.util.func.ThrowSupplier;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;
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
        static <T, E extends Throwable> @NotNull Consumer<T> rethrow(@NotNull ThrowConsumer<T, E> consumer) {
            return value -> {
                try {
                    consumer.accept(value);
                } catch (Throwable e) {
                    Rethrow.rethrow(e);
                }
            };
        }

        static <T, U, E extends Throwable> @NotNull BiConsumer<T, U> rethrow(@NotNull ThrowBiConsumer<T, U, E> consumer) {
            return (t, u) -> {
                try {
                    consumer.accept(t, u);
                } catch (Throwable e) {
                    Rethrow.rethrow(e);
                }
            };
        }
    }

    interface Suppliers {
        static <T, E extends Throwable> @NotNull Supplier<T> rethrow(@NotNull ThrowSupplier<T, E> supplier) {
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
        static <T, R, E extends Throwable> @NotNull Function<T, R> rethrow(@NotNull ThrowFunction<T, R, E> function) {
            return value -> {
                try {
                    return function.apply(value);
                } catch (Throwable e) {
                    return Rethrow.rethrow(e);
                }
            };
        }
    }

    interface Guava {
        @SuppressWarnings("Guava")
        @NotNull
        static <T, R, E extends Throwable> com.google.common.base.Function<T, R> rethrow(@NotNull ThrowFunction<T, R, E> function) {
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
