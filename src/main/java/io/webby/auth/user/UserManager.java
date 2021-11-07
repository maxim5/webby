package io.webby.auth.user;

import com.google.inject.Inject;
import io.webby.app.Settings;
import io.webby.db.kv.KeyValueDb;
import io.webby.db.kv.KeyValueFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static io.webby.util.base.EasyCast.castAny;

public class UserManager {
    private final KeyValueDb<Long, User> db;

    @Inject
    public UserManager(@NotNull Settings settings,
                       @NotNull UserFactory userFactory,
                       @NotNull KeyValueFactory dbFactory) {
        db = castAny(dbFactory.getDb("users", Long.class, userFactory.getUserClass()));
    }

    public @Nullable User findById(long id) {
        return db.get(id);
    }

    /*package*/ boolean tryInsert(@NotNull User user) {
        return db.putIfAbsent(user.userId(), user) == null;
    }

    /*package*/ long getMaxId() {
        return db.longSize();
    }
}
