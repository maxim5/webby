package io.webby.db.event;

import com.carrotsearch.hppc.IntHashSet;
import com.google.inject.Inject;
import io.webby.common.ManagedBy;
import io.webby.db.kv.DbOptions;
import io.webby.db.kv.KeyValueDb;
import io.webby.db.kv.KeyValueFactory;
import org.jetbrains.annotations.NotNull;

public class KeyCountersFactory {
    @Inject private KeyValueFactory factory;

    public @NotNull KeyIntSetCounter getIntCounter(@NotNull String name) {
        KeyValueDb<Integer, IntHashSet> db = factory.getDb(DbOptions.of(ManagedBy.BY_PROVIDER, name, Integer.class, IntHashSet.class));
        return new KeyIntSetCounter(db);
    }
}
