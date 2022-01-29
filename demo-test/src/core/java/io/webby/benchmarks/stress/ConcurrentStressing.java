package io.webby.benchmarks.stress;

import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ConcurrentStressing {
    public static final Duration INSTANT_WAIT = Duration.ofSeconds(1);
    public static final Duration QUICK_WAIT = Duration.ofMinutes(1);
    public static final Duration MEDIUM_WAIT = Duration.ofHours(1);
    public static final Duration LONG_WAIT = Duration.ofHours(10);

    public static void execWorkers(@NotNull Duration maxAwait,
                                   @NotNull List<? extends Runnable> workers) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(workers.size());
        workers.forEach(executor::execute);
        executor.shutdown();
        if (!executor.awaitTermination(maxAwait.toMillis(), TimeUnit.MILLISECONDS)) {
            executor.shutdownNow();
        }
    }
}
