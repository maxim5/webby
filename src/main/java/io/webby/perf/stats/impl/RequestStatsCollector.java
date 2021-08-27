package io.webby.perf.stats.impl;

import com.carrotsearch.hppc.IntIntHashMap;
import com.carrotsearch.hppc.IntIntMap;
import com.carrotsearch.hppc.IntObjectHashMap;
import com.carrotsearch.hppc.IntObjectMap;
import com.google.common.base.Stopwatch;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import io.webby.db.model.IntIdGenerator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

public class RequestStatsCollector {
    private static final IntIdGenerator generator = IntIdGenerator.positiveRandom(null);

    private final int id;
    private final HttpRequest request;
    private final Stopwatch stopwatch = Stopwatch.createStarted();
    private final AtomicLong lock = new AtomicLong(0);
    private final IntIntMap stats = new IntIntHashMap();
    private final IntObjectMap<List<StatsRecord>> records = new IntObjectHashMap<>();

    public RequestStatsCollector(int id, @NotNull HttpRequest request) {
        this.id = id;
        this.request = request;
    }

    public static @NotNull RequestStatsCollector create(@NotNull HttpRequest request) {
        return new RequestStatsCollector(generator.nextId(), request);
    }

    private static final DefaultHttpRequest REQUEST = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "");
    public static final RequestStatsCollector EMPTY =
        new RequestStatsCollector(0, REQUEST) {
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
        stats.addTo(key, count);
        StatsRecord record = new StatsRecord(elapsedMillis, hint);
        computeIfAbsent(records, key, ArrayList::new).add(record);
    }

    public int id() {
        return id;
    }

    public @NotNull HttpRequest request() {
        return request;
    }

    public @NotNull String uri() {
        return request.uri();
    }

    public long totalElapsed(@NotNull TimeUnit timeUnit) {
        return stopwatch.stop().elapsed(timeUnit);
    }

    public @NotNull IntIntMap stats() {
        return stats;
    }

    @Override
    public String toString() {
        return "[id=%08x, uri=%s]".formatted(id, request.uri());
    }

    private static <T> T computeIfAbsent(@NotNull IntObjectMap<T> map, int key, @NotNull Supplier<T> def) {
        T result = map.get(key);
        if (result == null) {
            result = def.get();
            map.put(key, result);
        }
        return result;
    }
}
