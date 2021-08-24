package io.webby.db.kv.lmdbjni;

import com.google.common.collect.Streams;
import io.webby.db.codec.Codec;
import io.webby.db.kv.KeyValueDb;
import io.webby.db.kv.impl.ByteArrayDb;
import org.fusesource.lmdbjni.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LmdbJniDb <K, V> extends ByteArrayDb<K, V> implements KeyValueDb<K, V> {
    private final Env env;
    private final Database db;

    public LmdbJniDb(@NotNull Env env, @NotNull Database db, @NotNull Codec<K> keyCodec, @NotNull Codec<V> valueCodec) {
        super(keyCodec, valueCodec);
        this.env = env;
        this.db = db;
    }

    @Override
    public int size() {
        return (int) longSize();
    }

    @Override
    public long longSize() {
        return db.stat().ms_entries;
    }

    @Override
    public @Nullable V get(@NotNull K key) {
        return asValue(db.get(fromKey(key)));
    }

    @Override
    public boolean containsValue(@NotNull V value) {
        byte[] bytes = fromValue(value);
        return iterate(iterator -> streamOf(iterator).map(Entry::getValue).anyMatch(val -> Arrays.equals(val, bytes)));
    }

    @Override
    public @NotNull Set<K> keySet() {
        return iterate(iterator -> streamOf(iterator).map(Entry::getKey).map(this::asKey).collect(Collectors.toSet()));
    }

    @Override
    public @NotNull Collection<V> values() {
        return iterate(iterator -> streamOf(iterator).map(Entry::getValue).map(this::asValue).toList());
    }

    @Override
    public @NotNull Set<Map.Entry<K, V>> entrySet() {
        return iterate(iterator -> {
            Set<Map.Entry<K, V>> result = new HashSet<>();
            for (Entry entry : iterator.iterable()) {
                result.add(asMapEntry(entry.getKey(), entry.getValue()));
            }
            return result;
        });
    }

    private <T> @NotNull T iterate(@NotNull Function<EntryIterator, T> converter) {
        try (Transaction transaction = env.createReadTransaction()) {
            try (EntryIterator iterator = db.iterate(transaction)) {
                return converter.apply(iterator);
            }
        }
    }

    @Override
    public void set(@NotNull K key, @NotNull V value) {
        db.put(fromKey(key), fromValue(value));
    }

    @Override
    public void delete(@NotNull K key) {
        db.delete(fromKey(key));
    }

    @Override
    public void clear() {
        try (Transaction transaction = env.createWriteTransaction()) {
            db.drop(transaction, false);
            transaction.commit();
        }
    }

    public @NotNull Database internalDb() {
        return db;
    }

    @Override
    public void flush() {
        env.sync(false);
    }

    @Override
    public void forceFlush() {
        env.sync(true);
    }

    @Override
    public void close() {
        db.close();
    }

    @SuppressWarnings("UnstableApiUsage")
    private static @NotNull Stream<Entry> streamOf(@NotNull EntryIterator iterator) {
        return Streams.stream(iterator);
    }
}
