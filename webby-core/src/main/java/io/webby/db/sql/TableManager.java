package io.webby.db.sql;

import com.google.common.collect.ImmutableMap;
import com.google.common.flogger.FluentLogger;
import com.google.common.primitives.Primitives;
import com.google.inject.Inject;
import io.webby.app.Settings;
import io.webby.common.ClasspathScanner;
import io.webby.common.Lifetime;
import io.webby.orm.api.*;
import io.webby.util.lazy.AtomicLazyRecycle;
import io.webby.util.lazy.LazyRecycle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Level;

import static io.webby.util.base.EasyCast.castAny;
import static java.util.Objects.requireNonNull;

public class TableManager implements HasEngine {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();
    private static final LazyRecycle<TableManager> SHARED_INSTANCE = AtomicLazyRecycle.createUninitialized();

    private final Connector connector;
    private final Engine engine;
    private final ImmutableMap<Class<?>, EntityTable> tableMap;

    @Inject
    public TableManager(@NotNull Settings settings,
                        @NotNull ConnectionPool pool,
                        @NotNull ClasspathScanner scanner,
                        @NotNull Lifetime lifetime) throws Exception {
        assert settings.storageSettings().isSqlEnabled() : "SQL storage is disabled";
        assert pool.isRunning() : "Invalid pool state: %s".formatted(pool);

        connector = new ThreadLocalConnector(pool, settings.getLongProperty("db.sql.connection.expiration.millis", 30_000));
        engine = pool.engine();

        Set<? extends Class<?>> tableClasses = scanner.getDerivedClasses(settings.modelFilter(), BaseTable.class);
        tableMap = buildTableMap(tableClasses);

        initializeStatic(this);
        lifetime.onTerminate(SHARED_INSTANCE::recycle);
    }

    public @NotNull Connector connector() {
        return connector;
    }

    @Override
    public @NotNull Engine engine() {
        return engine;
    }

    public <T extends BaseTable<?>> @Nullable T getTableOrNull(@NotNull Class<T> tableClass) {
        for (EntityTable entityTable : tableMap.values()) {
            if (entityTable.tableClass().equals(tableClass)) {
                return castAny(entityTable.instantiate.apply(connector));
            }
        }
        return null;
    }

    public <T extends BaseTable<?>> @NotNull T getTableOrDie(@NotNull Class<T> tableClass) {
        return requireNonNull(getTableOrNull(tableClass));
    }

    public @Nullable BaseTable<?> getTableOrNull(@NotNull String tableName) {
        for (EntityTable entityTable : tableMap.values()) {
            if (entityTable.tableName().equals(tableName)) {
                return castAny(entityTable.instantiate.apply(connector));
            }
        }
        return null;
    }

    public @NotNull BaseTable<?> getTableOrDie(@NotNull String tableName) {
        return requireNonNull(getTableOrNull(tableName));
    }

    public @Nullable BaseTable<?> getTableOrNull(@NotNull TableMeta meta) {
        return getTableOrNull(meta.sqlTableName());
    }

    public @NotNull BaseTable<?> getTableOrDie(@NotNull TableMeta meta) {
        return getTableOrDie(meta.sqlTableName());
    }

    public <E> @NotNull BaseTable<E> getMatchingBaseTableOrDie(@NotNull String name, @NotNull Class<? extends E> entity) {
        EntityTable entityTable = tableMap.get(entity);
        assert entityTable != null : "Entity table not found for: entity=%s".formatted(entity);
        assert entityTable.tableName().equals(name) :
                "Entity table name does not match: sql-name=%s name=%s".formatted(entityTable.tableName(), name);
        assert entityTable.key == null : "Key class mismatch: table-key=%s entity=%s".formatted(entityTable.key, entity);
        return castAny(entityTable.instantiate.apply(connector));
    }

    public <K, E> boolean hasMatchingTable(@NotNull String name,
                                           @NotNull Class<K> key,
                                           @NotNull Class<? extends E> entity) {
        EntityTable entityTable = tableMap.get(entity);
        return entityTable != null && entityTable.tableName().equals(name) && entityTable.wrappedKey() == Primitives.wrap(key);
    }

    public <K, E> @NotNull TableObj<K, E> getMatchingTableOrDie(@NotNull String name,
                                                                @NotNull Class<K> key,
                                                                @NotNull Class<? extends E> entity) {
        EntityTable entityTable = tableMap.get(entity);
        assert entityTable != null : "Entity table not found for: entity=%s".formatted(entity);
        assert entityTable.tableName().equals(name) :
                "Entity table name does not match: sql-name=%s name=%s".formatted(entityTable.tableName(), name);
        assert entityTable.wrappedKey() == Primitives.wrap(key) :
                "Key class mismatch: table-key=%s provided-key=%s entity=%s".formatted(entityTable.key, key, entity);
        return castAny(entityTable.instantiate.apply(connector));
    }

    @VisibleForTesting
    static ImmutableMap<Class<?>, EntityTable> buildTableMap(@NotNull Iterable<? extends Class<?>> tableClasses) throws Exception {
        ImmutableMap.Builder<Class<?>, EntityTable> result = new ImmutableMap.Builder<>();
        for (Class<?> tableClass : tableClasses) {
            TableMeta meta = castAny(tableClass.getField("META").get(null));
            Class<?> key = (Class<?>) tableClass.getField("KEY_CLASS").get(null);
            Class<?> entity = (Class<?>) tableClass.getField("ENTITY_CLASS").get(null);
            Function<Connector, ?> instantiate = castAny(tableClass.getField("INSTANTIATE").get(null));
            result.put(entity, new EntityTable(tableClass, meta, key, instantiate));
        }
        return result.buildOrThrow();
    }

    @NotNull List<TableMeta> getAllTables() {
        return tableMap.values().stream().map(EntityTable::meta).toList();
    }

    private record EntityTable(@NotNull Class<?> tableClass,
                               @NotNull TableMeta meta,
                               @Nullable Class<?> key,
                               @NotNull Function<Connector, ?> instantiate) {
        public @NotNull String tableName() {
            return meta.sqlTableName();
        }

        public @Nullable Class<?> wrappedKey() {
            return key != null ? Primitives.wrap(key) : null;
        }
    }

    public static @NotNull TableManager unsafeSharedInstance() {
        return SHARED_INSTANCE.getOrDie();
    }

    private static void initializeStatic(@NotNull TableManager instance) {
        if (SHARED_INSTANCE.isInitialized()) {
            log.at(Level.SEVERE).log("Table manager already statically initialized: `%s`. Call Lifetime.terminate() to clean-up",
                                     instance);
        } else {
            SHARED_INSTANCE.initializeOrDie(instance);
            log.at(Level.FINE).log("Initialized the table manager statically: `%s`", instance);
        }
    }
}
