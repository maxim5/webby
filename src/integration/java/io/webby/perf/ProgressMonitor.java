package io.webby.perf;

import com.google.common.flogger.FluentLogger;

import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;

public class ProgressMonitor {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    private final AtomicLong total = new AtomicLong();
    private final AtomicLong counter = new AtomicLong();

    public void expectTotalSteps(long steps) {
        assert steps >= 0 : "Invalid total steps: " + steps;
        total.addAndGet(steps);
    }

    public void step() {
        long count = counter.incrementAndGet();
        if (count % 1000_000 == 0) {
            long total = this.total.get();
            log.at(Level.INFO).log("Progress: %4.1f%% -> %d / %d", 100.0 * count / total, count, total);
        }
    }
}
