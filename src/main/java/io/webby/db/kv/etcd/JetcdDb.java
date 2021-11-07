package io.webby.db.kv.etcd;

import com.google.protobuf.UnsafeByteOperations;
import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.KV;
import io.etcd.jetcd.KeyValue;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.options.GetOption;
import io.webby.db.codec.Codec;
import io.webby.db.kv.KeyValueDb;
import io.webby.db.kv.impl.ByteArrayDb;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static io.webby.util.base.Rethrow.Suppliers.runRethrow;

public class JetcdDb<K, V> extends ByteArrayDb<K, V> implements KeyValueDb<K, V> {
    private final KV kv;
    private final ByteSequence namespace;
    private final boolean prefixed;

    public JetcdDb(@NotNull KV kv, @Nullable String name, @NotNull Codec<K> keyCodec, @NotNull Codec<V> valueCodec) {
        super(keyCodec, valueCodec);
        this.kv = kv;
        if (name != null) {
            namespace = wrapByteSequence("%s:".formatted(name).getBytes());
        } else {
            namespace = ByteSequence.from("\0", StandardCharsets.US_ASCII);
        }
        prefixed = name != null;
    }

    @Override
    public int size() {
        return runRethrow(() -> kv.get(namespace, withGetOptions(true)).get().getKvs()).size();
    }

    @Override
    public boolean isEmpty() {
        return runRethrow(() -> kv.get(namespace, withGetOptions(true)).get().getKvs()).isEmpty();
    }

    public @NotNull CompletableFuture<GetResponse> asyncGet(@NotNull K key) {
        return kv.get(wrapByteSequence(fromKey(key)));
    }

    @Override
    public @Nullable V get(@NotNull K key) {
        List<KeyValue> keyValues = runRethrow(() -> asyncGet(key).get().getKvs());
        return keyValues.isEmpty() ? null : toValue(keyValues.get(0).getValue());
    }

    @Override
    public boolean containsValue(@NotNull V value) {
        return values().contains(value);
    }

    @Override
    public @NotNull Set<K> keySet() {
        List<KeyValue> keyValues = runRethrow(() -> kv.get(namespace, withGetOptions(true)).get().getKvs());
        return keyValues.stream().map(KeyValue::getKey).map(this::toKey).collect(Collectors.toSet());
    }

    @Override
    public @NotNull Collection<V> values() {
        List<KeyValue> keyValues = runRethrow(() -> kv.get(namespace, withGetOptions(false)).get().getKvs());
        return keyValues.stream().map(KeyValue::getValue).map(this::toValue).toList();
    }

    @Override
    public @NotNull Set<Map.Entry<K, V>> entrySet() {
        List<KeyValue> keyValues = runRethrow(() -> kv.get(namespace, withGetOptions(false)).get().getKvs());
        return keyValues.stream()
                .map(entry -> asMapEntry(toKey(entry.getKey()), toValue(entry.getValue())))
                .collect(Collectors.toSet());
    }

    @Override
    public void set(@NotNull K key, @NotNull V value) {
        runRethrow(() -> kv.put(wrapByteSequence(fromKey(key)), wrapByteSequence(fromValue(value))).get());
    }

    @Override
    public void delete(@NotNull K key) {
        runRethrow(() -> kv.delete(wrapByteSequence(fromKey(key))).get());
    }

    @Override
    public void clear() {
        for (K key : keySet()) {
            delete(key);
        }
    }

    @Override
    public void flush() {
        // not available
    }

    @Override
    public void close() {
        kv.close();
    }

    public @NotNull KV internalClient() {
        return kv;
    }

    private @NotNull GetOption withGetOptions(boolean keysOnly) {
        return GetOption.newBuilder()
                .withKeysOnly(keysOnly)
                .withSortField(GetOption.SortTarget.KEY)
                .withSortOrder(GetOption.SortOrder.DESCEND)
                .withRange(namespace)
                .isPrefix(prefixed)
                .build();
    }

    @Override
    protected byte @NotNull [] fromKey(@NotNull K key) {
        return keyCodec.writeToBytes(namespace.getBytes(), key);
    }

    @Override
    protected @NotNull K asKeyNotNull(byte @NotNull [] bytes) {
        return keyCodec.readFromBytes(namespace.size(), bytes);
    }

    private @Nullable K toKey(@Nullable ByteSequence byteSequence) {
        return byteSequence != null ? asKey(byteSequence.getBytes()) : null;
    }

    private @Nullable V toValue(@Nullable ByteSequence byteSequence) {
        return byteSequence != null ? asValue(byteSequence.getBytes()) : null;
    }

    private static @NotNull ByteSequence wrapByteSequence(byte @NotNull [] bytes) {
        return ByteSequence.from(UnsafeByteOperations.unsafeWrap(bytes));
    }
}
