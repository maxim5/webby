package io.webby.util.base;

import com.google.common.base.Stopwatch;
import io.webby.util.func.ThrowRunnable;
import io.webby.util.func.ThrowSupplier;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;
import java.util.function.LongConsumer;
import java.util.function.ObjLongConsumer;
import java.util.function.Supplier;

public class TimeIt {
    public static void timeIt(@NotNull Runnable runnable, @NotNull LongConsumer timeConsumer) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        runnable.run();
        long elapsedMillis = stopwatch.stop().elapsed(TimeUnit.MILLISECONDS);
        timeConsumer.accept(elapsedMillis);
    }

    public static <E extends Throwable> void timeItOrDie(@NotNull ThrowRunnable<E> runnable,
                                                         @NotNull LongConsumer timeConsumer) throws E {
        Stopwatch stopwatch = Stopwatch.createStarted();
        runnable.run();
        long elapsedMillis = stopwatch.stop().elapsed(TimeUnit.MILLISECONDS);
        timeConsumer.accept(elapsedMillis);
    }

    public static <T> T timeIt(@NotNull Supplier<T> supplier, @NotNull ObjLongConsumer<T> timeConsumer) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        T result = supplier.get();
        long elapsedMillis = stopwatch.stop().elapsed(TimeUnit.MILLISECONDS);
        timeConsumer.accept(result, elapsedMillis);
        return result;
    }

    public static <T, E extends Throwable> T timeItOrDie(@NotNull ThrowSupplier<T, E> supplier,
                                                         @NotNull ObjLongConsumer<T> timeConsumer) throws E {
        Stopwatch stopwatch = Stopwatch.createStarted();
        T result = supplier.get();
        long elapsedMillis = stopwatch.stop().elapsed(TimeUnit.MILLISECONDS);
        timeConsumer.accept(result, elapsedMillis);
        return result;
    }
}
