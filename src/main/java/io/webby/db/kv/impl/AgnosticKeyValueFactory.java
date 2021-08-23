package io.webby.db.kv.impl;

import com.google.inject.Inject;
import io.webby.app.Settings;
import io.webby.common.InjectorHelper;
import io.webby.db.kv.KeyValueDb;
import io.webby.db.kv.KeyValueFactory;
import io.webby.db.kv.StorageType;
import io.webby.db.kv.chronicle.ChronicleFactory;
import io.webby.db.kv.javamap.JavaMapDbFactory;
import io.webby.db.kv.leveldbjni.LevelDbJniFactory;
import io.webby.db.kv.lmdbjava.LmdbJavaDbFactory;
import io.webby.db.kv.lmdbjni.LmdbJniDbFactory;
import io.webby.db.kv.mapdb.MapDbFactory;
import io.webby.db.kv.swaydb.SwayDbFactory;
import org.jetbrains.annotations.NotNull;

public class AgnosticKeyValueFactory implements KeyValueFactory {
    private final InjectorHelper helper;
    private final KeyValueFactory delegate;

    @Inject
    public AgnosticKeyValueFactory(@NotNull Settings settings, @NotNull InjectorHelper helper) {
        this.helper = helper;
        delegate = pickFactory(settings.storageType());
    }

    @Override
    public <K, V> @NotNull KeyValueDb<K, V> getDb(@NotNull String name, @NotNull Class<K> key, @NotNull Class<V> value) {
        return delegate.getDb(name, key, value);
    }

    public <K, V> @NotNull KeyValueDb<K, V> getCustomTypeDb(@NotNull StorageType storageType,
                                                            @NotNull String name,
                                                            @NotNull Class<K> key,
                                                            @NotNull Class<V> value) {
        return pickFactory(storageType).getDb(name, key, value);
    }

    private @NotNull KeyValueFactory pickFactory(@NotNull StorageType storageType) {
        return switch (storageType) {
            case JAVA_MAP -> helper.lazySingleton(JavaMapDbFactory.class);
            case MAP_DB -> helper.lazySingleton(MapDbFactory.class);
            case CHRONICLE_MAP -> helper.lazySingleton(ChronicleFactory.class);
            case LMDB_JAVA -> helper.lazySingleton(LmdbJavaDbFactory.class);
            case LMDB_JNI -> helper.lazySingleton(LmdbJniDbFactory.class);
            case SWAY_DB -> helper.lazySingleton(SwayDbFactory.class);
            case LEVEL_DB_JNI -> helper.lazySingleton(LevelDbJniFactory.class);
        };
    }
}
