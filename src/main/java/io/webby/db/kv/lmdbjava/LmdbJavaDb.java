package io.webby.db.kv.lmdbjava;

import io.netty.buffer.Unpooled;
import io.webby.db.codec.Codec;
import io.webby.db.kv.KeyValueDb;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lmdbjava.CursorIterable;
import org.lmdbjava.Dbi;
import org.lmdbjava.Env;
import org.lmdbjava.Txn;

import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;

public class LmdbJavaDb<K, V> implements KeyValueDb<K, V> {
    private final Env<ByteBuffer> env;
    private final Dbi<ByteBuffer> db;
    private final Codec<K> keyCodec;
    private final Codec<V> valueCodec;

    public LmdbJavaDb(@NotNull Env<ByteBuffer> env, @NotNull Dbi<ByteBuffer> db,
                      @NotNull Codec<K> keyCodec, @NotNull Codec<V> valueCodec) {
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
        try (Txn<ByteBuffer> txn = env.txnRead()) {
            return db.stat(txn).entries;
        }
    }

    @Override
    public @Nullable V get(@NotNull K key) {
        try (Txn<ByteBuffer> txn = env.txnRead()) {
            return asValue(db.get(txn, fromKey(key)));
        }
    }

    @Override
    public boolean containsValue(@NotNull V value) {
        ByteBuffer buffer = fromValue(value);
        try (Txn<ByteBuffer> txn = env.txnRead()) {
            try (CursorIterable<ByteBuffer> iterable = db.iterate(txn)) {
                for (CursorIterable.KeyVal<ByteBuffer> entry : iterable) {
                    if (entry.val().equals(buffer)) {
                        return true;
                    }
                }
                return false;
            }
        }
    }

    @Override
    public @NotNull Set<K> keySet() {
        try (Txn<ByteBuffer> txn = env.txnRead()) {
            try (CursorIterable<ByteBuffer> iterable = db.iterate(txn)) {
                Set<K> keys = new HashSet<>();
                for (CursorIterable.KeyVal<ByteBuffer> entry : iterable) {
                    keys.add(asKey(entry.key()));
                }
                return keys;
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
        try (Txn<ByteBuffer> txn = env.txnWrite()) {
            db.drop(txn, false);
            txn.commit();
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

    private @NotNull ByteBuffer fromKey(@NotNull K key) {
        byte[] bytes = keyCodec.writeToBytes(key);
        return ByteBuffer.allocateDirect(bytes.length).put(bytes).flip();
    }

    private @Nullable K asKey(@Nullable ByteBuffer buffer) {
        return buffer == null ? null : keyCodec.readFrom(Unpooled.wrappedBuffer(buffer));
    }

    private @NotNull ByteBuffer fromValue(@NotNull V value) {
        byte[] bytes = valueCodec.writeToBytes(value);
        return ByteBuffer.allocateDirect(bytes.length).put(bytes).flip();
    }

    private @Nullable V asValue(@Nullable ByteBuffer buffer) {
        return buffer == null ? null : valueCodec.readFrom(Unpooled.wrappedBuffer(buffer));
    }
}
