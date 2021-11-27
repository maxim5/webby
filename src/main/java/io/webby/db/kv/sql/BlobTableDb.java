package io.webby.db.kv.sql;

import com.google.common.io.BaseEncoding;
import io.webby.db.codec.Codec;
import io.webby.db.kv.KeyValueDb;
import io.webby.db.kv.impl.ByteArrayDb;
import io.webby.db.model.BlobKv;
import io.webby.orm.api.TableObj;
import io.webby.orm.api.query.Compare;
import io.webby.orm.api.query.Func;
import io.webby.orm.api.query.HardcodedStringTerm;
import io.webby.orm.api.query.Where;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static io.webby.orm.api.query.CompareType.EQ;
import static io.webby.orm.api.query.Shortcuts.like;
import static io.webby.orm.api.query.Shortcuts.literal;

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
            case MySQL, SQLite -> Where.of(like(ID_COLUMN, literal(namespace + "%")));
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
        Compare compare = switch (table.engine()) {
            // Use the "?" arg to avoid query overflow
            case SQLite -> EQ.compare(Func.HEX.of(VALUE_COLUMN), literal(upperhex(fromValue(value))));
            case MySQL, H2 -> null; // EQ.compare(VALUE_COLUMN, blob(fromValue(value)));
            default -> throw new UnsupportedOperationException("containsValue() not implemented for SQL engine:" + table.engine());
        };

        if (compare != null) {
            return table.count(Where.and(whereNamespace, compare)) > 0;
        }
        return table.fetchMatching(whereNamespace).stream().map(BlobKv::value).map(this::asValue).anyMatch(value::equals);
    }

    @Override
    public @NotNull Iterable<K> keys() {
        return table.fetchMatching(whereNamespace).stream().map(BlobKv::id).map(this::asKey).toList();
    }

    @Override
    public @NotNull Set<K> keySet() {
        return table.fetchMatching(whereNamespace).stream().map(BlobKv::id).map(this::asKey).collect(Collectors.toSet());
    }

    @Override
    public @NotNull Collection<V> values() {
        return table.fetchMatching(whereNamespace).stream().map(BlobKv::value).map(this::asValue).toList();
    }

    @Override
    public @NotNull Set<Map.Entry<K, V>> entrySet() {
        return table.fetchMatching(whereNamespace).stream()
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
