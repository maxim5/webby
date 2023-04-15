package io.webby.db.kv.impl;

import com.google.common.collect.Streams;
import io.webby.db.kv.KeyValueDb;
import io.webby.util.func.Reversible;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class GenericKeyValueDbAdapter<K1, V1, K2, V2> implements KeyValueDb<K1, V1> {
    private final KeyValueDb<K2, V2> delegate;
    private final Reversible<K1, K2> keyFunc;
    private final Reversible<V1, V2> valueFunc;

    public GenericKeyValueDbAdapter(@NotNull KeyValueDb<K2, V2> delegate,
                                    @NotNull Reversible<K1, K2> keyFunc,
                                    @NotNull Reversible<V1, V2> valueFunc) {
        this.delegate = delegate;
        this.keyFunc = keyFunc;
        this.valueFunc = valueFunc;
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public @Nullable V1 get(@NotNull K1 key) {
        return valueFunc.backwardNullable(delegate.get(keyFunc.forward(key)));
    }

    @Override
    public boolean containsValue(@NotNull V1 value) {
        return delegate.containsValue(valueFunc.forward(value));
    }

    @Override
    public @NotNull Iterable<K1> keys() {
        return Streams.stream(delegate.keys()).map(keyFunc.reverse()).toList();
    }

    @Override
    public @NotNull Set<K1> keySet() {
        return Streams.stream(delegate.keys()).map(keyFunc.reverse()).collect(Collectors.toSet());
    }

    @Override
    public @NotNull Collection<V1> values() {
        return delegate.values().stream().map(valueFunc.reverse()).toList();
    }

    @Override
    public @NotNull Set<Map.Entry<K1, V1>> entrySet() {
        return delegate.entrySet().stream()
            .map(entry -> new AbstractMap.SimpleEntry<>(
                keyFunc.backwardNullable(entry.getKey()),
                valueFunc.backwardNullable(entry.getValue())))
            .collect(Collectors.toSet());
    }

    @Override
    public void set(@NotNull K1 key, @NotNull V1 value) {
        delegate.set(keyFunc.forward(key), valueFunc.forward(value));
    }

    @Override
    public void delete(@NotNull K1 key) {
        delegate.delete(keyFunc.forward(key));
    }

    @Override
    public void clear() {
        delegate.clear();
    }

    @Override
    public void flush() {
        delegate.flush();
    }

    @Override
    public void close() {
        delegate.close();
    }
}
