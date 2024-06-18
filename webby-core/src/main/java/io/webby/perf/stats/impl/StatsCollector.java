package io.webby.perf.stats.impl;

import com.carrotsearch.hppc.IntIntHashMap;
import com.carrotsearch.hppc.IntIntMap;
import com.carrotsearch.hppc.IntObjectHashMap;
import com.carrotsearch.hppc.IntObjectMap;
import com.carrotsearch.hppc.cursors.IntIntCursor;
import com.google.common.base.Stopwatch;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.webby.db.model.IntIdGenerator;
import io.webby.perf.stats.Stat;
import io.spbx.util.hppc.EasyHppc;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class StatsCollector {
    private static final IntIdGenerator generator = IntIdGenerator.positiveRandom(null);

    private final int id;
    private final Stopwatch totalStopwatch = Stopwatch.createStarted();
    private final AtomicLong lock = new AtomicLong(0);
    private final IntIntMap mainCounts = new IntIntHashMap();
    private final IntObjectMap<List<StatsRecord>> records = new IntObjectHashMap<>();

    public StatsCollector(int id) {
        this.id = id;
    }

    public static @NotNull StatsCollector createWithRandomId() {
        return new StatsCollector(generator.nextId());
    }

    public int id() {
        return id;
    }

    public @NotNull IntIntMap mainCounts() {
        return mainCounts;
    }

    public @NotNull IntObjectMap<List<StatsRecord>> records() {
        return records;
    }

    public void forEach(@NotNull StatsConsumer consumer) {
        for (IntIntCursor cursor : mainCounts) {
            Stat stat = Stat.index().findStatOrDummy(cursor.key);
            List<StatsRecord> statsRecords = records.getOrDefault(cursor.key, Collections.emptyList());
            consumer.consume(stat, cursor.value, statsRecords);
        }
    }

    public boolean lock() {
        assert isActive() : "Attempt to lock a stopped collector: " + this;
        return lock.compareAndSet(0, System.currentTimeMillis());
    }

    @CanIgnoreReturnValue
    public long unlock() {
        assert isActive() : "Attempt to unlock a stopped collector: " + this;
        return lock.getAndSet(0);
    }

    public void unlockAndReport(int key, int count, @Nullable Object hint) {
        long millis = unlock();
        long elapsedMillis = System.currentTimeMillis() - millis;
        report(key, count, elapsedMillis, hint);
    }

    public void report(int key, int count, long elapsedMillis, @Nullable Object hint) {
        assert isActive() : "Attempt to add report to a stopped collector: " + this;
        mainCounts.addTo(key, count);
        StatsRecord record = new StatsRecord(elapsedMillis, hint);
        EasyHppc.computeIfAbsent(records, key, ArrayList::new).add(record);
    }

    public boolean isActive() {
        return totalStopwatch.isRunning();
    }

    @CanIgnoreReturnValue
    public @NotNull StatsCollector stop() {
        assert lock.get() == 0 : "Attempt to stop a locked collector: " + this;
        if (totalStopwatch.isRunning()) {
            totalStopwatch.stop();
        }
        return this;
    }

    public long totalElapsed(@NotNull TimeUnit timeUnit) {
        return totalStopwatch.elapsed(timeUnit);
    }

    @Override
    public String toString() {
        return "[id=%08x]".formatted(id);
    }

    public interface StatsConsumer {
        void consume(@NotNull Stat key, int value, @NotNull List<StatsRecord> records);
    }
}
