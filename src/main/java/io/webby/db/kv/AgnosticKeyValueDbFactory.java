package io.webby.db.kv;

import com.google.inject.Inject;
import com.google.inject.Injector;
import io.webby.db.kv.mapdb.MapDbFactory;
import org.jetbrains.annotations.NotNull;

public class AgnosticKeyValueDbFactory implements KeyValueDbFactory {
    private final KeyValueDbFactory delegate;

    @Inject
    public AgnosticKeyValueDbFactory(@NotNull Injector injector) {
        StorageType type = StorageType.MAP_DB;  // TODO: settings
        delegate = switch (type) {
            case MAP_DB -> injector.getInstance(MapDbFactory.class);
        };
    }

    @Override
    @NotNull
    public <K, V> KeyValueDb<K, V> getDb(@NotNull String name, @NotNull Class<K> key, @NotNull Class<V> value) {
        return delegate.getDb(name, key, value);
    }
}
