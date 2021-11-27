package io.webby.db.sql;

import com.google.common.collect.ImmutableMap;
import com.google.common.flogger.FluentLogger;
import com.google.common.primitives.Primitives;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.webby.app.Settings;
import io.webby.common.ClasspathScanner;
import io.webby.common.Lifetime;
import io.webby.orm.api.*;
import io.webby.orm.codegen.SqlSchemaMaker;
import io.webby.util.lazy.ResettableAtomicLazy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import java.sql.SQLException;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Level;

import static io.webby.util.base.EasyCast.castAny;
import static io.webby.util.base.Rethrow.rethrow;

@Singleton
public class TableManager {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();
    private static final ResettableAtomicLazy<TableManager> SHARED_INSTANCE = new ResettableAtomicLazy<>();

    private final Settings settings;
    private final Connector connector;
    private final ImmutableMap<Class<?>, EntityTable> tableMap;

    @Inject
    public TableManager(@NotNull Settings settings,
                        @NotNull ConnectionPool pool,
                        @NotNull ClasspathScanner scanner,
                        @NotNull Lifetime lifetime) throws Exception {
        assert settings.storageSettings().isSqlStorageEnabled() : "SQL storage is disabled";
        assert pool.isRunning() : "Invalid pool state: %s".formatted(pool);

        this.settings = settings;
        connector = new ThreadLocalConnector(pool, settings.getLongProperty("db.sql.connection.expiration.millis", 30_000));

        Set<? extends Class<?>> tableClasses = scanner.getDerivedClasses(settings.modelFilter(), BaseTable.class);
        tableMap = buildTableMap(tableClasses);

        initializeStatic(this);
        lifetime.onTerminate(SHARED_INSTANCE::reset);
    }

    public @NotNull Connector connector() {
        return connector;
    }

    public @Nullable BaseTable<?> getTableByNameOrNull(@NotNull String name) {
        for (EntityTable entityTable : tableMap.values()) {
            if (entityTable.tableName().equals(name)) {
                return castAny(entityTable.instantiate.apply(connector));
            }
        }
        return null;
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
            result.put(entity, new EntityTable(meta, key, instantiate));
        }
        return result.buildOrThrow();
    }

    public void createAllTablesIfNotExist() {
        if (settings.isProdMode()) {
            log.at(Level.WARNING).log("Automatic SQL table creation called in production");
        }

        Engine engine = connector.engine();
        try {
            for (EntityTable entityTable : tableMap.values()) {
                log.at(Level.FINE).log("Creating SQL table if not exists: `%s`...", entityTable.tableName());
                String query = SqlSchemaMaker.makeCreateTableQuery(engine, entityTable.meta());
                connector().runner().runMultiUpdate(query);
            }
        } catch (SQLException e) {
            rethrow(e);
        }
    }

    private record EntityTable(@NotNull TableMeta meta,
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
