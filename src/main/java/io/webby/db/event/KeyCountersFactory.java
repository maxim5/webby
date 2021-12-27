package io.webby.db.event;

import com.carrotsearch.hppc.IntHashSet;
import com.google.inject.Inject;
import io.webby.common.Lifetime;
import io.webby.db.kv.DbOptions;
import io.webby.db.kv.KeyValueDb;
import io.webby.db.kv.KeyValueFactory;
import org.jetbrains.annotations.NotNull;

public class KeyCountersFactory {
    @Inject private KeyValueFactory factory;
    @Inject private Lifetime lifetime;

    public @NotNull KeyIntSetCounter getIntCounter(@NotNull String name) {
        KeyValueDb<Integer, IntHashSet> db = factory.getDb(DbOptions.of(name, Integer.class, IntHashSet.class));
        KeyIntSetCounter counter = new KeyIntSetCounter(db);
        lifetime.onTerminate(counter);
        return counter;
    }
}
