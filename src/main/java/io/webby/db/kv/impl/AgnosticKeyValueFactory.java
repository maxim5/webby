package io.webby.db.kv.impl;

import com.google.inject.Inject;
import io.webby.app.Settings;
import io.webby.app.StorageSettings;
import io.webby.common.InjectorHelper;
import io.webby.db.kv.KeyValueDb;
import io.webby.db.kv.KeyValueFactory;
import io.webby.db.kv.KeyValueSettings;
import io.webby.db.kv.StorageType;
import io.webby.db.kv.chronicle.ChronicleFactory;
import io.webby.db.kv.halodb.HaloDbFactory;
import io.webby.db.kv.javamap.JavaMapDbFactory;
import io.webby.db.kv.leveldb.LevelDbIq80Factory;
import io.webby.db.kv.leveldb.LevelDbJniFactory;
import io.webby.db.kv.lmdbjava.LmdbJavaDbFactory;
import io.webby.db.kv.lmdbjni.LmdbJniDbFactory;
import io.webby.db.kv.mapdb.MapDbFactory;
import io.webby.db.kv.oak.OakFactory;
import io.webby.db.kv.paldb.PalDbFactory;
import io.webby.db.kv.redis.JedisDbFactory;
import io.webby.db.kv.rocksdb.RocksDbFactory;
import io.webby.db.kv.sql.SqlTableDbFactory;
import io.webby.db.kv.swaydb.SwayDbFactory;
import io.webby.db.kv.tupl.TuplFactory;
import org.jetbrains.annotations.NotNull;

import static io.webby.util.base.EasyCast.castAny;

public class AgnosticKeyValueFactory implements KeyValueFactory {
    private final InjectorHelper helper;
    private final KeyValueFactory delegate;

    @Inject
    public AgnosticKeyValueFactory(@NotNull Settings settings, @NotNull InjectorHelper helper) {
        this.helper = helper;
        delegate = pickFactory(getStorageType(settings.storageSettings()));
    }

    @Override
    public <K, V> @NotNull KeyValueDb<K, V> getDb(@NotNull String name, @NotNull Class<K> key, @NotNull Class<V> value) {
        return delegate.getDb(name, key, value);
    }

    public <K, V> @NotNull KeyValueDb<K, V> getDb(@NotNull StorageType storageType, @NotNull String name,
                                                  @NotNull Class<K> key, @NotNull Class<V> value) {
        return pickFactory(storageType).getDb(name, key, value);
    }

    public <F extends InternalKeyValueFactory> @NotNull F getInternalFactory(@NotNull StorageType storageType) {
        return castAny(pickFactory(storageType));
    }

    private static @NotNull StorageType getStorageType(@NotNull StorageSettings storage) {
        return storage.isKeyValueEnabled() ? storage.keyValueSettingsOrDie().type() : KeyValueSettings.DEFAULT_TYPE;
    }

    private @NotNull InternalKeyValueFactory pickFactory(@NotNull StorageType storageType) {
        Class<? extends InternalKeyValueFactory> factoryClass = switch (storageType) {
            case CHRONICLE_MAP -> ChronicleFactory.class;
            case HALO_DB -> HaloDbFactory.class;
            case JAVA_MAP -> JavaMapDbFactory.class;
            case JEDIS -> JedisDbFactory.class;
            case LEVEL_DB_IQ80 -> LevelDbIq80Factory.class;
            case LEVEL_DB_JNI -> LevelDbJniFactory.class;
            case LMDB_JAVA -> LmdbJavaDbFactory.class;
            case LMDB_JNI -> LmdbJniDbFactory.class;
            case MAP_DB -> MapDbFactory.class;
            case OAK -> OakFactory.class;
            case PAL_DB -> PalDbFactory.class;
            case ROCKS_DB -> RocksDbFactory.class;
            case SWAY_DB -> SwayDbFactory.class;
            case SQL_DB -> SqlTableDbFactory.class;
            case TUPL -> TuplFactory.class;
        };
        return castAny(helper.lazySingleton(factoryClass));
    }
}
