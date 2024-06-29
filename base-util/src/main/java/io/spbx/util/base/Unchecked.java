package io.spbx.util.base;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.spbx.util.func.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class Unchecked {
    @CanIgnoreReturnValue
    public static <T> T rethrow(@NotNull Throwable exception) {
        throw new RuntimeException(exception);
    }

    @CanIgnoreReturnValue
    public static <T> T rethrow(@NotNull String message, @NotNull Throwable exception) {
        throw new RuntimeException(message, exception);
    }

    @CanIgnoreReturnValue
    public static <T> T rethrow(@NotNull IOException exception) {
        throw new UncheckedIOException(exception);
    }

    @CanIgnoreReturnValue
    public static <T> T rethrow(@NotNull String message, @NotNull IOException exception) {
        throw new UncheckedIOException(message, exception);
    }

    // Idea taken from
    // https://stackoverflow.com/questions/4519557/is-there-a-way-to-throw-an-exception-without-adding-the-throws-declaration
    @SuppressWarnings("unchecked")
    @CanIgnoreReturnValue
    public static <T extends Throwable, R> R throwAny(Throwable exception) throws T {
        throw (T) exception;
    }

    public static class Runnables {
        public static <E extends Throwable> @NotNull Runnable rethrow(@NotNull ThrowRunnable<E> consumer) {
            return () -> {
                try {
                    consumer.run();
                } catch (RuntimeException | Error e) {
                    throw e;
                } catch (Throwable e) {
                    Unchecked.rethrow(e);
                }
            };
        }

        public static <E extends Throwable> void runRethrow(@NotNull ThrowRunnable<E> action) {
            try {
                action.run();
            } catch (RuntimeException | Error e) {
                throw e;
            } catch (Throwable e) {
                Unchecked.rethrow(e);
            }
        }
    }

    public static class Consumers {
        public static <T, E extends Throwable> @NotNull Consumer<T> rethrow(@NotNull ThrowConsumer<T, E> consumer) {
            return value -> {
                try {
                    consumer.accept(value);
                } catch (RuntimeException | Error e) {
                    throw e;
                } catch (Throwable e) {
                    Unchecked.rethrow(e);
                }
            };
        }

        public static <T, U, E extends Throwable>
                @NotNull BiConsumer<T, U> rethrow(@NotNull ThrowBiConsumer<T, U, E> consumer) {
            return (t, u) -> {
                try {
                    consumer.accept(t, u);
                } catch (RuntimeException | Error e) {
                    throw e;
                } catch (Throwable e) {
                    Unchecked.rethrow(e);
                }
            };
        }
    }

    public static class Suppliers {
        public static <T, E extends Throwable> @NotNull Supplier<T> rethrow(@NotNull ThrowSupplier<T, E> supplier) {
            return () -> {
                try {
                    return supplier.get();
                } catch (RuntimeException | Error e) {
                    throw e;
                } catch (Throwable e) {
                    return Unchecked.rethrow(e);
                }
            };
        }

        public static <T, E extends Throwable> T runRethrow(@NotNull ThrowSupplier<T, E> action) {
            try {
                return action.get();
            } catch (RuntimeException | Error e) {
                throw e;
            } catch (Throwable e) {
                return Unchecked.rethrow(e);
            }
        }
    }

    public static class Functions {
        public static <T, R, E extends Throwable> @NotNull Function<T, R> rethrow(@NotNull ThrowFunction<T, R, E> func) {
            return value -> {
                try {
                    return func.apply(value);
                } catch (RuntimeException | Error e) {
                    throw e;
                } catch (Throwable e) {
                    return Unchecked.rethrow(e);
                }
            };
        }
    }

    public static class Guava {
        @SuppressWarnings("Guava")
        public static @NotNull <T, R, E extends Throwable>
                com.google.common.base.Function<T, R> rethrow(@NotNull ThrowFunction<T, R, E> func) {
            return value -> {
                try {
                    return func.apply(value);
                } catch (RuntimeException | Error e) {
                    throw e;
                } catch (Throwable e) {
                    return Unchecked.rethrow(e);
                }
            };
        }
    }
}
