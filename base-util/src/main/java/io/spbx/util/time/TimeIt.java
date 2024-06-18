package io.spbx.util.time;

import com.google.common.base.Stopwatch;
import io.spbx.util.func.ThrowRunnable;
import io.spbx.util.func.ThrowSupplier;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;
import java.util.function.LongConsumer;
import java.util.function.ObjLongConsumer;

/**
 * A helper utility for running the action and measuring elapsed time.
 */
public class TimeIt {
    @CheckReturnValue
    public static <E extends Throwable> @NotNull MillisListener timeIt(@NotNull ThrowRunnable<E> runnable) throws E {
        Stopwatch stopwatch = Stopwatch.createStarted();
        runnable.run();
        long elapsedMillis = stopwatch.stop().elapsed(TimeUnit.MILLISECONDS);
        return timeConsumer -> timeConsumer.accept(elapsedMillis);
    }

    @CheckReturnValue
    public static <T, E extends Throwable> @NotNull ObjMillisListener<T> timeIt(@NotNull ThrowSupplier<T, E> supplier) throws E {
        Stopwatch stopwatch = Stopwatch.createStarted();
        T result = supplier.get();
        long elapsedMillis = stopwatch.stop().elapsed(TimeUnit.MILLISECONDS);
        return timeConsumer -> {
            timeConsumer.accept(result, elapsedMillis);
            return result;
        };
    }

    @FunctionalInterface
    public interface MillisListener {
        void onDone(@NotNull LongConsumer timeConsumer);
    }

    @FunctionalInterface
    public interface ObjMillisListener<T> {
        T onDone(@NotNull ObjLongConsumer<T> timeConsumer);
    }
}
