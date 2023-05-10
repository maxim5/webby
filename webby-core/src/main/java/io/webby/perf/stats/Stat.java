package io.webby.perf.stats;

import com.carrotsearch.hppc.IntObjectHashMap;
import com.carrotsearch.hppc.IntObjectMap;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.Immutable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public record Stat(@NotNull String name, int key, Unit unit) {
    private static final List<Stat> REGISTERED_STATS = Collections.synchronizedList(new ArrayList<>());
    private static final AtomicReference<Index> INDEX = new AtomicReference<>();

    public static final Stat DB_GET = registerStat("db_get", 1, Unit.CALLS);
    public static final Stat DB_SET = registerStat("db_set", 2, Unit.CALLS);
    public static final Stat DB_DELETE = registerStat("db_delete", 3, Unit.CALLS);
    public static final Stat DB_SIZE = registerStat("db_size", 4, Unit.CALLS);
    public static final Stat DB_SCAN = registerStat("db_scan", 5, Unit.CALLS);
    public static final Stat DB_IO = registerStat("db_io", 6, Unit.CALLS);

    public static final Stat CODEC_READ = registerStat("codec_read", 101, Unit.BYTES);
    public static final Stat CODEC_WRITE = registerStat("codec_write", 102, Unit.BYTES);

    public static final Stat RENDER = registerStat("render", 200, Unit.BYTES);

    public static @NotNull Stat registerStat(@NotNull String name, int key, Unit unit) {
        Stat stat = new Stat(name, key, unit);
        REGISTERED_STATS.add(stat);
        maybeUpdateIndex();
        return stat;
    }

    @CanIgnoreReturnValue
    public static boolean unregisterStat(@NotNull Stat stat) {
        boolean removed = REGISTERED_STATS.remove(stat);
        if (removed) {
            maybeUpdateIndex();
        }
        return removed;
    }

    private static void maybeUpdateIndex() {
        Index index = INDEX.get();
        if (index != null) {
            Index newIndex = Index.buildFromList(REGISTERED_STATS);
            if (!INDEX.compareAndSet(index, newIndex)) {
                throw new ConcurrentModificationException("Concurrent index update");
            }
        }
    }

    public static @NotNull Index index() {
        Index index = INDEX.get();
        if (index == null) {
            index = Index.buildFromList(REGISTERED_STATS);
            if (!INDEX.compareAndSet(null, index)) {
                return INDEX.get();
            }
        }
        return index;
    }

    public enum Unit {
        CALLS,
        BYTES,
        NONE;

        public String lowerName() {
            return this == NONE ? "" : name().toLowerCase();
        }
    }

    @Immutable
    public static class Index {
        private final IntObjectMap<Stat> values;
        private final IntObjectMap<String> names;
        private final int maxNameLength;

        private Index(@NotNull IntObjectMap<Stat> values, @NotNull IntObjectMap<String> names, int maxNameLength) {
            this.values = values;
            this.names = names;
            this.maxNameLength = maxNameLength;
        }

        private static @NotNull Index buildFromList(@NotNull List<Stat> stats) {
            IntObjectMap<Stat> values = new IntObjectHashMap<>(stats.size());
            IntObjectMap<String> names = new IntObjectHashMap<>(stats.size());
            int maxNameLength = 0;
            for (Stat stat : stats) {
                Stat prev = values.put(stat.key, stat);
                assert prev == null : "Stats with duplicate keys found: %s vs %s".formatted(prev, stat);
                String name = names.put(stat.key, stat.name);
                assert name == null : "Internal error. Duplicate found: stat=%s, name=%s".formatted(stat, name);
                maxNameLength = Math.max(maxNameLength, stat.name.length());
            }
            return new Index(values, names, maxNameLength);
        }

        public int size() {
            assert values.size() == names.size() : "Internal error. Size match: values=%s vs names=%s".formatted(values, names);
            return values.size();
        }

        public @Nullable Stat findStatOrNull(int key) {
            return values.get(key);
        }

        public @NotNull Stat findStatOrDummy(int key) {
            Stat stat = findStatOrNull(key);
            return stat != null ? stat : new Stat(String.valueOf(key), key, Unit.NONE);
        }

        public @Nullable String findNameOrNull(int key) {
            return names.get(key);
        }

        public @NotNull String findNameOrDummy(int key) {
            String name = findNameOrNull(key);
            return name != null ? name : String.valueOf(key);
        }

        public int maxNameLength() {
            return maxNameLength;
        }
    }
}
