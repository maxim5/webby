package io.webby.benchmarks.stress;

import com.google.common.flogger.FluentLogger;

import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;

public class ProgressMonitor {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();
    private static final long DEFAULT_REPORT_PAUSE_MILLIS = 10_000;

    private final AtomicLong total = new AtomicLong();
    private final AtomicLong counter = new AtomicLong();
    private final AtomicLong nextReportTimestamp = new AtomicLong();
    private final long startMillis = System.currentTimeMillis();
    private final long reportPauseMillis;

    public ProgressMonitor(long reportPauseMillis) {
        this.reportPauseMillis = reportPauseMillis;
    }

    public ProgressMonitor() {
        this(DEFAULT_REPORT_PAUSE_MILLIS);
    }

    public void expectTotalSteps(long steps) {
        assert steps >= 0 : "Invalid total steps: " + steps;
        total.addAndGet(steps);
    }

    public void excludeRemaining(long steps) {
        assert steps >= 0 : "Invalid steps to exclude: " + steps;
        total.addAndGet(-steps);
    }

    public void step() {
        long count = counter.incrementAndGet();
        long now = System.currentTimeMillis();
        long current = nextReportTimestamp.get();
        if (current < now && nextReportTimestamp.compareAndSet(current, now + reportPauseMillis)) {
            long total = this.total.get();
            log.at(Level.INFO).log("Progress: %4.1f%% -> %d / %d after %d sec",
                                   100.0 * count / total, count, total, (now - startMillis) / 1000);
        }
    }
}
