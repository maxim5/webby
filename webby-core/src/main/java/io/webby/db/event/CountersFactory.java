package io.webby.db.event;

import com.carrotsearch.hppc.IntHashSet;
import com.google.inject.Inject;
import io.webby.common.Lifetime;
import io.webby.db.kv.DbOptions;
import io.webby.db.kv.KeyValueDb;
import io.webby.db.kv.KeyValueFactory;
import org.jetbrains.annotations.NotNull;

public class CountersFactory {
    @Inject private KeyValueFactory factory;
    @Inject private Lifetime lifetime;

    public @NotNull IntCounter getIntCounter(@NotNull String name) {
        KeyValueDb<Integer, Integer> db = factory.getDb(DbOptions.of(name, Integer.class, Integer.class));
        IntCounter counter = new IntCounter(db);
        lifetime.onTerminate(counter);
        return counter;
    }

    public @NotNull IntSetCounter getIntSetCounter(@NotNull String name) {
        KeyValueDb<Integer, IntHashSet> db = factory.getDb(DbOptions.of(name, Integer.class, IntHashSet.class));
        IntSetCounter counter = new IntSetCounter(db);
        lifetime.onTerminate(counter);
        return counter;
    }
}
