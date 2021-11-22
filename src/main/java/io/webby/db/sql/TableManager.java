package io.webby.db.sql;

import com.google.common.primitives.Primitives;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.webby.app.Settings;
import io.webby.common.ClasspathScanner;
import io.webby.orm.api.BaseTable;
import io.webby.orm.api.TableMeta;
import io.webby.orm.api.TableObj;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import java.sql.Connection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static io.webby.util.base.EasyCast.castAny;

@Singleton
public class TableManager {
    private final ConnectionPool pool;
    private final Map<Class<?>, EntityTable> tableMap;

    @Inject
    public TableManager(@NotNull Settings settings,
                        @NotNull ConnectionPool pool,
                        @NotNull ClasspathScanner scanner) throws Exception {
        this.pool = pool;

        Set<? extends Class<?>> tableClasses = scanner.getDerivedClasses(settings.modelFilter(), BaseTable.class);
        this.tableMap = buildTableMap(tableClasses);
    }

    public <E> @NotNull BaseTable<E> getMatchingBaseTableOrDie(@NotNull String name, @NotNull Class<? extends E> entity) {
        EntityTable entityTable = tableMap.get(entity);
        assert entityTable != null : "Entity table not found for: entity=%s".formatted(entity);
        assert entityTable.sqlName.equals(name) :
                "Entity table name does not match: sql-name=%s name=%s".formatted(entityTable.sqlName, name);
        assert entityTable.key == null : "Key class mismatch: table-key=%s entity=%s".formatted(entityTable.key, entity);
        return castAny(entityTable.instantiate.apply(pool.getConnection()));
    }

    public <K, E> boolean hasMatchingTable(@NotNull String name,
                                           @NotNull Class<K> key,
                                           @NotNull Class<? extends E> entity) {
        EntityTable entityTable = tableMap.get(entity);
        return entityTable != null && entityTable.sqlName.equals(name) && entityTable.wrappedKey() == Primitives.wrap(key);
    }

    public <K, E> @NotNull TableObj<K, E> getMatchingTableOrDie(@NotNull String name,
                                                                @NotNull Class<K> key,
                                                                @NotNull Class<? extends E> entity) {
        EntityTable entityTable = tableMap.get(entity);
        assert entityTable != null : "Entity table not found for: entity=%s".formatted(entity);
        assert entityTable.sqlName.equals(name) :
                "Entity table name does not match: sql-name=%s name=%s".formatted(entityTable.sqlName, name);
        assert entityTable.wrappedKey() == Primitives.wrap(key) :
                "Key class mismatch: table-key=%s provided-key=%s entity=%s".formatted(entityTable.key, key, entity);
        return castAny(entityTable.instantiate.apply(pool.getConnection()));
    }

    @VisibleForTesting
    static Map<Class<?>, EntityTable> buildTableMap(@NotNull Iterable<? extends Class<?>> tableClasses) throws Exception {
        Map<Class<?>, EntityTable> result = new LinkedHashMap<>();
        for (Class<?> tableClass : tableClasses) {
            TableMeta meta = castAny(tableClass.getField("META").get(null));
            Class<?> key = (Class<?>) tableClass.getField("KEY_CLASS").get(null);
            Class<?> entity = (Class<?>) tableClass.getField("ENTITY_CLASS").get(null);
            Function<Connection, ?> instantiate = castAny(tableClass.getField("INSTANTIATE").get(null));
            result.put(entity, new EntityTable(meta.sqlTableName(), key, instantiate));
        }
        return result;
    }

    private record EntityTable(@NotNull String sqlName,
                               @Nullable Class<?> key,
                               @NotNull Function<Connection, ?> instantiate) {
        public @Nullable Class<?> wrappedKey() {
            return key != null ? Primitives.wrap(key) : null;
        }
    }
}
