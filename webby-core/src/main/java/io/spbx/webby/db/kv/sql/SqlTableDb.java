package io.spbx.webby.db.kv.sql;

import io.spbx.orm.api.TableObj;
import io.spbx.webby.db.kv.KeyValueDb;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class SqlTableDb<K, V> implements KeyValueDb<K, V> {
    private final TableObj<K, V> table;

    public SqlTableDb(@NotNull TableObj<K, V> table) {
        this.table = table;
    }

    @Override
    public int size() {
        return table.count();
    }

    @Override
    public boolean isEmpty() {
        return table.isEmpty();
    }

    @Override
    public @Nullable V get(@NotNull K key) {
        return table.getByPkOrNull(key);
    }

    @Override
    public boolean containsValue(@NotNull V value) {
        return value.equals(get(table.keyOf(value)));
    }

    @Override
    public void forEach(@NotNull BiConsumer<? super K, ? super V> action) {
        table.forEach(value -> action.accept(table.keyOf(value), value));
    }

    @Override
    public @NotNull Iterable<K> keys() {
        return table.fetchAll().stream().map(table::keyOf).toList();
    }

    @Override
    public @NotNull Set<K> keySet() {
        return table.fetchAll().stream().map(table::keyOf).collect(Collectors.toSet());
    }

    @Override
    public @NotNull Collection<V> values() {
        return table.fetchAll();
    }

    @Override
    public @NotNull Set<Map.Entry<K, V>> entrySet() {
        return table.fetchAll().stream()
            .map(entity -> new AbstractMap.SimpleEntry<>(table.keyOf(entity), entity))
            .collect(Collectors.toSet());
    }

    @Override
    public void set(@NotNull K key, @NotNull V value) {
        assert key.equals(table.keyOf(value)) : "Inconsistent key=`%s` and value=`%s`".formatted(key, value);
        table.updateByPkOrInsert(value);
    }

    @Override
    public void delete(@NotNull K key) {
        table.deleteByPk(key);
    }

    @Override
    public void clear() {
        for (K key : keys()) {
            table.deleteByPk(key);
        }
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() {
    }
}
