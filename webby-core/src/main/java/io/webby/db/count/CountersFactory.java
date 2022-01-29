package io.webby.db.count;

import com.carrotsearch.hppc.IntHashSet;
import com.google.inject.Inject;
import io.webby.common.Lifetime;
import io.webby.db.count.impl.KvIntSetStorageImpl;
import io.webby.db.count.impl.LockBasedIntSetCounter;
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

    // TODO[!]: options for impl
    public @NotNull IntSetCounter getIntSetCounter(@NotNull String name) {
        KeyValueDb<Integer, IntHashSet> db = factory.getDb(DbOptions.of(name, Integer.class, IntHashSet.class));
        LockBasedIntSetCounter counter = new LockBasedIntSetCounter(new KvIntSetStorageImpl(db));
        lifetime.onTerminate(counter);
        return counter;
    }
}
