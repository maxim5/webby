package io.webby.db.kv.sql;

import com.google.common.primitives.Bytes;
import io.webby.db.codec.Codec;
import io.webby.db.kv.KeyValueDb;
import io.webby.db.kv.impl.ByteArrayDb;
import io.webby.db.model.BlobKv;
import io.webby.util.sql.api.TableObj;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BlobTableDb<V, K> extends ByteArrayDb<K, V> implements KeyValueDb<K, V> {
    private final TableObj<byte[], BlobKv> table;
    private final byte[] namespace;

    public BlobTableDb(@NotNull TableObj<byte[], BlobKv> table,
                       @NotNull String namespace,
                       @NotNull Codec<K> keyCodec,
                       @NotNull Codec<V> valueCodec) {
        super(keyCodec, valueCodec);
        this.table = table;
        this.namespace = "%s:".formatted(namespace).getBytes();
    }

    @Override
    public int size() {
        return (int) fetchAllFromNamespace().count();  // TODO: filter namespace
    }

    @Override
    public @Nullable V get(@NotNull K key) {
        BlobKv blob = table.getByPkOrNull(fromKey(key));
        return blob != null ? asValue(blob.value()) : null;
    }

    @Override
    public boolean containsValue(@NotNull V value) {
        return values().contains(value);  // TODO: selectByValue
    }

    @Override
    public @NotNull Iterable<K> keys() {
        return fetchAllFromNamespace().map(BlobKv::id).map(this::asKey).toList();
    }

    @Override
    public @NotNull Set<K> keySet() {
        return fetchAllFromNamespace().map(BlobKv::id).map(this::asKey).collect(Collectors.toSet());
    }

    @Override
    public @NotNull Collection<V> values() {
        return fetchAllFromNamespace().map(BlobKv::value).map(this::asValue).toList();
    }

    @Override
    public @NotNull Set<Map.Entry<K, V>> entrySet() {
        return fetchAllFromNamespace()
                .map(entity -> new AbstractMap.SimpleEntry<>(asKey(entity.id()), asValue(entity.value())))
                .collect(Collectors.toSet());
    }

    @Override
    public void set(@NotNull K key, @NotNull V value) {
        BlobKv blob = new BlobKv(fromKey(key), fromValue(value));
        table.updateByPkOrInsert(blob);
    }

    @Override
    public void delete(@NotNull K key) {
        table.deleteByPk(fromKey(key));
    }

    @Override
    public void clear() {
        for (K key : keys()) {
            delete(key);
        }
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() {
    }

    @Override
    protected byte @NotNull [] fromKey(@NotNull K key) {
        return keyCodec.writeToBytes(namespace, key);
    }

    @Override
    protected @NotNull K asKeyNotNull(byte @NotNull [] bytes) {
        return keyCodec.readFromBytes(namespace.length, bytes);
    }

    private @NotNull Stream<BlobKv> fetchAllFromNamespace() {
        return table.fetchAll().stream().filter(blob -> startsWith(blob.id(), namespace));
    }

    // TODO: https://github.com/patrickfav/bytes-java
    private static boolean startsWith(byte[] bytes, byte[] prefix) {
        return Bytes.indexOf(bytes, prefix) == 0;
    }
}
