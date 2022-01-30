package io.webby.db.count;

import com.google.inject.Inject;
import io.webby.app.Settings;
import io.webby.common.Lifetime;
import io.webby.db.kv.DbOptions;
import io.webby.db.kv.KeyValueDb;
import io.webby.db.kv.KeyValueFactory;
import org.jetbrains.annotations.NotNull;

public class IntCounterFactory {
    @Inject private Settings settings;
    @Inject private KeyValueFactory keyValueFactory;
    @Inject private Lifetime lifetime;

    public @NotNull IntCounter getIntCounter(@NotNull String name) {
        KeyValueDb<Integer, Integer> db = keyValueFactory.getDb(DbOptions.of(name, Integer.class, Integer.class));
        IntCounter counter = new IntCounter(db);
        lifetime.onTerminate(counter);
        return counter;
    }
}
