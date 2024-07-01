package io.spbx.webby.db.kv.halodb;

import com.oath.halodb.HaloDB;
import com.oath.halodb.HaloDBIterator;
import com.oath.halodb.Record;
import com.oath.halodb.RecordKey;
import io.spbx.util.base.Unchecked.Consumers;
import io.spbx.webby.db.codec.Codec;
import io.spbx.webby.db.kv.KeyValueDb;
import io.spbx.webby.db.kv.impl.ByteArrayDb;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static io.spbx.util.base.Unchecked.Runnables.runRethrow;
import static io.spbx.util.base.Unchecked.Suppliers.runRethrow;
import static io.spbx.webby.db.kv.impl.KeyValueCommons.streamOf;

public class HaloDbImpl<K, V> extends ByteArrayDb<K, V> implements KeyValueDb<K, V> {
    private final HaloDB db;

    public HaloDbImpl(@NotNull HaloDB db, @NotNull Codec<K> keyCodec, @NotNull Codec<V> valueCodec) {
        super(keyCodec, valueCodec);
        this.db = db;
    }

    @Override
    public int size() {
        return (int) longSize();
    }

    @Override
    public long longSize() {
        return db.size();
    }

    @Override
    public @Nullable V get(@NotNull K key) {
        return runRethrow(() -> asValue(db.get(fromKey(key))));
    }

    @Override
    public boolean containsValue(@NotNull V value) {
        return streamOf(dbIterator()).map(Record::getValue).map(this::asValue).anyMatch(value::equals);
    }

    @Override
    public @NotNull Iterable<K> keys() {
        return streamOf(db.newKeyIterator()).map(RecordKey::getBytes).map(this::asKey).toList();
    }

    @Override
    public @NotNull Set<K> keySet() {
        return streamOf(db.newKeyIterator()).map(RecordKey::getBytes).map(this::asKey).collect(Collectors.toSet());
    }

    @Override
    public @NotNull Collection<V> values() {
        return streamOf(dbIterator()).map(Record::getValue).map(this::asValue).toList();
    }

    @Override
    public @NotNull Set<Map.Entry<K, V>> entrySet() {
        return streamOf(dbIterator())
            .map(record -> asMapEntry(record.getKey(), record.getValue()))
            .collect(Collectors.toSet());
    }

    @Override
    public void set(@NotNull K key, @NotNull V value) {
        runRethrow(() -> db.put(fromKey(key), fromValue(value)));
    }

    @Override
    public void delete(@NotNull K key) {
        runRethrow(() -> db.delete(fromKey(key)));
    }

    @Override
    public void clear() {
        streamOf(db.newKeyIterator()).map(RecordKey::getBytes).forEach(Consumers.rethrow(db::delete));
    }

    @Override
    public void flush() {
        // unsupported
    }

    @Override
    public void close() {
        runRethrow(db::close);
    }

    public @NotNull HaloDB internalDb() {
        return db;
    }

    private @NotNull HaloDBIterator dbIterator() {
        return runRethrow(db::newIterator);
    }
}
