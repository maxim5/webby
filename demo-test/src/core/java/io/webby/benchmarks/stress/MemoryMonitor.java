package io.webby.benchmarks.stress;

import com.google.common.flogger.FluentLogger;
import io.webby.testing.TestingUtil;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.stream.IntStream;

// https://stackoverflow.com/questions/17374743/how-can-i-get-the-memory-that-my-java-program-uses-via-javas-runtime-api
public class MemoryMonitor implements Runnable {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();
    private static final long MB = 1 << 20;

    private static final long DEFAULT_SAMPLE_PAUSE_MILLIS = 1000L;
    private static final int DEFAULT_SAMPLES_TO_KEEP = 1024;

    private final AtomicBoolean stop = new AtomicBoolean();
    private final long samplePauseMillis;
    private final Sample[] samples;
    private int tail = 0;

    public MemoryMonitor(int samplesToKeep, long samplePauseMillis) {
        samples = new Sample[samplesToKeep];
        this.samplePauseMillis = samplePauseMillis;
    }

    public MemoryMonitor(long samplePauseMillis) {
        this(DEFAULT_SAMPLES_TO_KEEP, samplePauseMillis);
    }

    public MemoryMonitor() {
        this(DEFAULT_SAMPLES_TO_KEEP, DEFAULT_SAMPLE_PAUSE_MILLIS);
    }

    public void startDaemon() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::buildSummaryReport));

        Thread thread = new Thread(this, "Memory-Monitor");
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public void run() {
        Runtime runtime = Runtime.getRuntime();

        Sample previous = new Sample(System.currentTimeMillis(), runtime.totalMemory(), runtime.freeMemory());
        add(previous);
        while (!stop.get()) {
            long total = runtime.totalMemory();
            long free = runtime.freeMemory();
            if (total != previous.total || free != previous.free) {
                Sample next = new Sample(System.currentTimeMillis(), total, free);
                add(next);
                previous = next;
            }
            TestingUtil.waitFor(samplePauseMillis);
        }
    }

    public void buildSummaryReport() {
        stop.set(true);

        List<Sample> series = IntStream.range(0, samples.length)
                .mapToObj(i -> samples[(i + tail + 1) % samples.length])
                .filter(Objects::nonNull).toList();

        System.out.println("\n[MEMORY] Summary report:");
        System.out.println("Time                       Used Mb   Used %");
        int step = Math.max(series.size() / 10, 1);
        for (int i = 0; i < series.size(); i += step) {
            Sample sample = series.get(i);
            System.out.printf("%s   %4dm      %04.1f%%%n", sample.instantFormatted(), sample.used() / MB, sample.usedRatio());
        }
        // System.out.println("Mean: " + series.stream().mapToDouble(Sample::freeRatio).sum() / series.size());
    }

    private void add(Sample sample) {
        samples[++tail % samples.length] = sample;
        log.at(Level.INFO).log("[MEMORY] used: %04.1f%%  %4dm / %4dm",
                               sample.usedRatio(), sample.used() / MB, sample.total() / MB);
    }

    private record Sample(long timestampMillis, long total, long free) {
        private static final DateTimeFormatter DATE_TIME_FORMATTER =
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS").withZone(ZoneId.systemDefault());

        public long used() {
            return total - free;
        }

        public double freeRatio() {
            return 100.0 * free / total;
        }

        public double usedRatio() {
            return 100.0 * used() / total;
        }

        public @NotNull Instant instant() {
            return Instant.ofEpochMilli(timestampMillis);
        }

        public @NotNull String instantFormatted() {
            return DATE_TIME_FORMATTER.format(instant());
        }
    }
}
