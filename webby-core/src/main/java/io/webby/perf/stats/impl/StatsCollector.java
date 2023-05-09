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
import io.webby.util.hppc.EasyHppc;
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
    private final Stopwatch stopwatch = Stopwatch.createStarted();
    private final AtomicLong lock = new AtomicLong(0);
    private final IntIntMap main = new IntIntHashMap();
    private final IntObjectMap<List<StatsRecord>> records = new IntObjectHashMap<>();

    public StatsCollector(int id) {
        this.id = id;
    }

    public static @NotNull StatsCollector createWithRandomId() {
        return new StatsCollector(generator.nextId());
    }

    static final StatsCollector EMPTY =
        new StatsCollector(0) {
            @Override
            public boolean lock() {
                return false;
            }
        };

    public boolean lock() {
        return lock.compareAndSet(0, System.currentTimeMillis());
    }

    @CanIgnoreReturnValue
    public long unlock() {
        return lock.getAndSet(0);
    }

    public void unlock(int key, int count, @Nullable Object hint) {
        long millis = unlock();
        long elapsedMillis = System.currentTimeMillis() - millis;
        report(key, count, elapsedMillis, hint);
    }

    public void report(int key, int count, long elapsedMillis, @Nullable Object hint) {
        main.addTo(key, count);
        StatsRecord record = new StatsRecord(elapsedMillis, hint);
        EasyHppc.computeIfAbsent(records, key, ArrayList::new).add(record);
    }

    public @NotNull StatsCollector stop() {
        if (stopwatch.isRunning()) {
            stopwatch.stop();
        }
        return this;
    }

    public int id() {
        return id;
    }

    public long totalElapsed(@NotNull TimeUnit timeUnit) {
        return stopwatch.elapsed(timeUnit);
    }

    public @NotNull IntIntMap main() {
        return main;
    }

    public @NotNull IntObjectMap<List<StatsRecord>> records() {
        return records;
    }

    public void forEach(@NotNull StatsConsumer consumer) {
        for (IntIntCursor cursor : main) {
            Stat stat = Stat.VALUES.get(cursor.key);
            List<StatsRecord> statsRecords = records.getOrDefault(cursor.key, Collections.emptyList());
            consumer.consume(stat, cursor.value, statsRecords);
        }
    }

    @Override
    public String toString() {
        return "[id=%08x]".formatted(id);
    }

    public interface StatsConsumer {
        void consume(@NotNull Stat key, int value, @NotNull List<StatsRecord> records);
    }
}
