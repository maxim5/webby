package io.webby.db.kv.sql;

import com.google.inject.Inject;
import io.webby.db.kv.impl.BaseKeyValueFactory;
import io.webby.db.sql.TableManager;
import io.webby.util.sql.api.TableObj;
import org.jetbrains.annotations.NotNull;

public class SqlTableDbFactory extends BaseKeyValueFactory {
    @Inject private TableManager tableManager;

    @Override
    public @NotNull <K, V> SqlTableDb<K, V> getInternalDb(@NotNull String name, @NotNull Class<K> key, @NotNull Class<V> value) {
        return cacheIfAbsent(name, () -> {
            TableObj<K, V> table = tableManager.getTable(key, value);
            return new SqlTableDb<>(table);
        });
    }
}
