package io.webby.perf;

import com.carrotsearch.hppc.IntIntHashMap;
import com.carrotsearch.hppc.IntIntMap;
import com.carrotsearch.hppc.ObjectIntHashMap;
import com.carrotsearch.hppc.ObjectIntMap;
import com.google.common.base.Stopwatch;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import io.webby.db.model.LongIdGenerator;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicBoolean;

public class RequestStatsCollector {
    private static final LongIdGenerator generator = LongIdGenerator.positiveRandom(null);

    private final long id;
    private final HttpRequest request;
    private final Stopwatch stopwatch;
    private final AtomicBoolean lock;
    private final IntIntMap intStats;
    private final ObjectIntMap<String> strStats;

    public RequestStatsCollector(long id,
                                 @NotNull HttpRequest request,
                                 @NotNull Stopwatch stopwatch,
                                 @NotNull AtomicBoolean lock,
                                 @NotNull IntIntMap intStats,
                                 @NotNull ObjectIntMap<String> strStats) {
        this.id = id;
        this.request = request;
        this.stopwatch = stopwatch;
        this.lock = lock;
        this.intStats = intStats;
        this.strStats = strStats;
    }

    public static @NotNull RequestStatsCollector create(@NotNull HttpRequest request) {
        return new RequestStatsCollector(generator.nextId(), request, Stopwatch.createStarted(),
                                         new AtomicBoolean(), new IntIntHashMap(), new ObjectIntHashMap<>());
    }

    private static final DefaultHttpRequest REQUEST = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "");
    public static final RequestStatsCollector EMPTY =
        new RequestStatsCollector(0, REQUEST, Stopwatch.createUnstarted(), new AtomicBoolean(),
                                  new IntIntHashMap(), new ObjectIntHashMap<>()) {
            @Override
            public boolean lock() {
                return false;
            }

            @Override
            public void unlock() {
            }
        };

    public boolean lock() {
        return lock.compareAndSet(false, true);
    }

    public void unlock() {
        lock.set(false);
    }

    public long id() {
        return id;
    }

    public @NotNull HttpRequest request() {
        return request;
    }

    public @NotNull Stopwatch stopwatch() {
        return stopwatch;
    }

    public @NotNull IntIntMap intStats() {
        return intStats;
    }

    public @NotNull ObjectIntMap<String> strStats() {
        return strStats;
    }

    @Override
    public String toString() {
        return "RequestStatsCollector[id=%d, request=%s]".formatted(id, request);
    }
}
