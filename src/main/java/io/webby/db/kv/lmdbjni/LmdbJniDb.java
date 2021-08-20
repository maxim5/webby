package io.webby.db.kv.lmdbjni;

import com.google.common.collect.Streams;
import io.netty.buffer.Unpooled;
import io.webby.db.codec.Codec;
import io.webby.db.kv.KeyValueDb;
import org.fusesource.lmdbjni.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class LmdbJniDb <K, V> implements KeyValueDb<K, V> {
    private final Env env;
    private final Database db;
    private final Codec<K> keyCodec;
    private final Codec<V> valueCodec;

    public LmdbJniDb(@NotNull Env env, @NotNull Database db, @NotNull Codec<K> keyCodec, @NotNull Codec<V> valueCodec) {
        this.env = env;
        this.db = db;
        this.keyCodec = keyCodec;
        this.valueCodec = valueCodec;
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

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public boolean containsValue(@NotNull V value) {
        byte[] bytes = fromValue(value);
        try (Transaction transaction = env.createReadTransaction()) {
            try (EntryIterator iterator = db.iterate(transaction)) {
                return Streams.stream(iterator).map(Entry::getValue).anyMatch(val -> Arrays.equals(val, bytes));
            }
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public @NotNull Set<K> keySet() {
        try (Transaction transaction = env.createReadTransaction()) {
            try (EntryIterator iterator = db.iterate(transaction)) {
                return Streams.stream(iterator).map(Entry::getKey).map(this::asKey).collect(Collectors.toSet());
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

    private byte @NotNull [] fromKey(@NotNull K key) {
        return keyCodec.writeToBytes(key);
    }

    private @Nullable K asKey(byte @Nullable [] bytes) {
        return bytes == null ? null : keyCodec.readFrom(Unpooled.wrappedBuffer(bytes));
    }

    private byte @NotNull [] fromValue(@NotNull V value) {
        return valueCodec.writeToBytes(value);
    }

    private @Nullable V asValue(byte @Nullable [] bytes) {
        return bytes == null ? null : valueCodec.readFrom(Unpooled.wrappedBuffer(bytes));
    }
}
