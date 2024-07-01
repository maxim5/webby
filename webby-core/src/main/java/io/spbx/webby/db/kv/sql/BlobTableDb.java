package io.spbx.webby.db.kv.sql;

import com.google.common.io.BaseEncoding;
import io.spbx.orm.api.TableObj;
import io.spbx.orm.api.query.Compare;
import io.spbx.orm.api.query.Func;
import io.spbx.orm.api.query.HardcodedStringTerm;
import io.spbx.orm.api.query.Where;
import io.spbx.webby.db.codec.Codec;
import io.spbx.webby.db.kv.KeyValueDb;
import io.spbx.webby.db.kv.impl.ByteArrayDb;
import io.spbx.webby.db.model.BlobKv;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static io.spbx.orm.api.query.CompareType.EQ;
import static io.spbx.orm.api.query.Shortcuts.*;

public class BlobTableDb<V, K> extends ByteArrayDb<K, V> implements KeyValueDb<K, V> {
    // Hardcoding the table columns to avoid a dep on the generated BlobKvTable from the core...
    private static final HardcodedStringTerm ID_COLUMN = new HardcodedStringTerm("id");
    private static final HardcodedStringTerm VALUE_COLUMN = new HardcodedStringTerm("value");

    private final TableObj<byte[], BlobKv> table;
    private final byte[] namespace;
    private final Where whereNamespace;

    public BlobTableDb(@NotNull TableObj<byte[], BlobKv> table,
                       @NotNull String name,
                       @NotNull Codec<K> keyCodec,
                       @NotNull Codec<V> valueCodec) {
        super(keyCodec, valueCodec);
        this.table = table;

        String namespace = "%s:".formatted(name);
        this.namespace = namespace.getBytes();
        this.whereNamespace = switch (table.engine()) {
            case H2 -> Where.of(like(ID_COLUMN, literal(lowerhex(this.namespace) + "%")));
            case MySQL, MariaDB, SQLite -> Where.of(like(ID_COLUMN, literal(namespace + "%")));
            default -> throw new UnsupportedOperationException("BlobTableDb not implemented for SQL engine: " + table.engine());
        };
    }

    @Override
    public int size() {
        return table.count(whereNamespace);
    }

    @Override
    public @Nullable V get(@NotNull K key) {
        BlobKv blob = table.getByPkOrNull(fromKey(key));
        return blob != null ? asValue(blob.value()) : null;
    }

    @Override
    public boolean containsValue(@NotNull V value) {
        byte[] bytes = fromValue(value);
        Compare compare = switch (table.engine()) {
            case SQLite -> EQ.compare(Func.HEX.apply(VALUE_COLUMN), var(upperhex(bytes)));
            case MySQL, MariaDB, H2 -> EQ.compare(VALUE_COLUMN, var(bytes));
            default -> throw new UnsupportedOperationException("containsValue() not implemented for SQL engine:" + table.engine());
        };

        if (compare != null) {
            return table.count(Where.and(whereNamespace, compare)) > 0;
        }
        return table.fetchAllMatching(whereNamespace).stream().map(BlobKv::value).map(this::asValue).anyMatch(value::equals);
    }

    @Override
    public @NotNull Iterable<K> keys() {
        return table.fetchAllMatching(whereNamespace).stream().map(BlobKv::id).map(this::asKey).toList();
    }

    @Override
    public @NotNull Set<K> keySet() {
        return table.fetchAllMatching(whereNamespace).stream().map(BlobKv::id).map(this::asKey).collect(Collectors.toSet());
    }

    @Override
    public @NotNull Collection<V> values() {
        return table.fetchAllMatching(whereNamespace).stream().map(BlobKv::value).map(this::asValue).toList();
    }

    @Override
    public @NotNull Set<Map.Entry<K, V>> entrySet() {
        return table.fetchAllMatching(whereNamespace).stream()
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

    public @NotNull TableObj<byte[], BlobKv> internalTable() {
        return table;
    }

    @Override
    protected byte @NotNull [] fromKey(@NotNull K key) {
        return keyCodec.writeToBytes(namespace, key);
    }

    @Override
    protected @NotNull K asKeyNotNull(byte @NotNull [] bytes) {
        return keyCodec.readFromBytes(namespace.length, bytes);
    }

    private static @NotNull String upperhex(byte @NotNull [] bytes) {
        return BaseEncoding.base16().upperCase().encode(bytes);
    }

    private static @NotNull String lowerhex(byte @NotNull [] bytes) {
        return BaseEncoding.base16().lowerCase().encode(bytes);
    }
}
