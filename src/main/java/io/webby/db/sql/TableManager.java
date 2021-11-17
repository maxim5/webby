package io.webby.db.sql;

import com.google.common.primitives.Primitives;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.webby.common.ClasspathScanner;
import io.webby.util.sql.api.BaseTable;
import io.webby.util.sql.api.TableObj;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Function;

import static io.webby.util.base.EasyCast.castAny;

@Singleton
public class TableManager {
    private final ConnectionPool pool;
    private final Map<Class<?>, EntityTable> tableMap;

    @Inject
    public TableManager(@NotNull ConnectionPool pool, @NotNull ClasspathScanner scanner) throws Exception {
        this.pool = pool;
        this.tableMap = scanTableClasses(scanner);
    }

    public <E> @NotNull BaseTable<E> getBaseTable(@NotNull Class<E> entity) {
        EntityTable entityTable = tableMap.get(entity);
        assert entityTable != null : "Entity table not found for: entity=%s".formatted(entity);
        assert entityTable.key == null : "Key class mismatch: table-key=%s entity=%s".formatted(entityTable.key, entity);
        return castAny(entityTable.instantiate.apply(pool.getConnection()));
    }

    public <K, E> @NotNull TableObj<K, E> getTable(@NotNull Class<K> key, @NotNull Class<E> entity) {
        EntityTable entityTable = tableMap.get(entity);
        assert entityTable != null : "Entity table not found for: entity=%s".formatted(entity);
        assert entityTable.wrappedKey() == Primitives.wrap(key) :
                "Key class mismatch: table-key=%s provided-key=%s entity=%s".formatted(entityTable.key, key, entity);
        return castAny(entityTable.instantiate.apply(pool.getConnection()));
    }

    @Inject
    private static Map<Class<?>, EntityTable> scanTableClasses(@NotNull ClasspathScanner scanner) throws Exception {
        BiPredicate<String, String> filter = (pkg, cls) -> pkg.startsWith("io.webby"); // TODO: temp, use settings filter
        Set<? extends Class<?>> tableClasses = scanner.getDerivedClasses(filter, BaseTable.class);

        Map<Class<?>, EntityTable> result = new LinkedHashMap<>();
        for (Class<?> tableClass : tableClasses) {
            Class<?> key = (Class<?>) tableClass.getField("KEY_CLASS").get(null);
            Class<?> entity = (Class<?>) tableClass.getField("ENTITY_CLASS").get(null);
            Function<Connection, ?> instantiate = castAny(tableClass.getField("INSTANTIATE").get(null));
            result.put(entity, new EntityTable(key, instantiate));
        }
        return result;
    }

    private record EntityTable(@Nullable Class<?> key, @NotNull Function<Connection, ?> instantiate) {
        public @Nullable Class<?> wrappedKey() {
            return key != null ? Primitives.wrap(key) : null;
        }
    }
}
