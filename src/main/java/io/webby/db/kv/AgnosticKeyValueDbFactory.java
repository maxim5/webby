package io.webby.db.kv;

import com.google.inject.Inject;
import io.webby.common.InjectorHelper;
import io.webby.db.kv.mapdb.MapDbFactory;
import org.jetbrains.annotations.NotNull;

public class AgnosticKeyValueDbFactory implements KeyValueDbFactory {
    private final KeyValueDbFactory delegate;

    @Inject
    public AgnosticKeyValueDbFactory(@NotNull InjectorHelper helper) {
        StorageType type = StorageType.MAP_DB;  // TODO: settings
        delegate = switch (type) {
            case MAP_DB -> helper.lazySingleton(MapDbFactory.class);
        };
    }

    @Override
    @NotNull
    public <K, V> KeyValueDb<K, V> getDb(@NotNull String name, @NotNull Class<K> key, @NotNull Class<V> value) {
        return delegate.getDb(name, key, value);
    }
}
