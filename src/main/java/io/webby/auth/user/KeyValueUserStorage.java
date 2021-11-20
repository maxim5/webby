package io.webby.auth.user;

import com.google.inject.Inject;
import io.webby.app.Settings;
import io.webby.db.kv.KeyValueDb;
import io.webby.db.kv.KeyValueFactory;
import io.webby.db.model.LongIdGenerator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static io.webby.util.base.EasyCast.castAny;

public class KeyValueUserStorage implements UserStorage {
    private final KeyValueDb<Long, User> db;
    private final LongIdGenerator generator;

    @Inject
    public KeyValueUserStorage(@NotNull Settings settings, @NotNull KeyValueFactory dbFactory) {
        // TODO settings: user class, generator type
        Class<?> userClass = DefaultUser.class;
        db = castAny(dbFactory.getDb(User.DB_NAME, Long.class, userClass));
        generator = LongIdGenerator.autoIncrement(this::getMaxId);
    }

    @Override
    public @Nullable User findByUserId(long userId) {
        return db.get(userId);
    }

    @Override
    public long createUserAutoId(@NotNull User user) {
        for (int i = 0; i < 5; i++) {
            user.resetIdToAuto();
            long userId = generator.nextId();
            user.setIfAutoIdOrDie(userId);
            if (tryInsert(userId, user)) {
                return userId;
            }
        }
        throw new RuntimeException("Too many failed attempts to create a user");
    }

    private boolean tryInsert(long userId, @NotNull User user) {
        return db.putIfAbsent(userId, user) == null;
    }

    private long getMaxId() {
        return db.longSize() + 1;
    }
}
