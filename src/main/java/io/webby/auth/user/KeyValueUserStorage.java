package io.webby.auth.user;

import com.google.inject.Inject;
import io.webby.app.Settings;
import io.webby.db.kv.DbOptions;
import io.webby.db.kv.KeyValueDb;
import io.webby.db.kv.KeyValueFactory;
import io.webby.db.model.IntIdGenerator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static io.webby.util.base.EasyCast.castAny;

public class KeyValueUserStorage implements UserStorage {
    private final KeyValueDb<Integer, User> db;
    private final IntIdGenerator generator;

    @Inject
    public KeyValueUserStorage(@NotNull Settings settings,
                               @NotNull Class<? extends User> userClass,
                               @NotNull KeyValueFactory dbFactory) {
        boolean randomIds = settings.getBoolProperty("user.id.generator.random.enabled");
        db = castAny(dbFactory.getDb(DbOptions.of(User.DB_NAME, Integer.class, userClass)));
        generator = randomIds ? IntIdGenerator.random(null) : IntIdGenerator.autoIncrement(() -> db.size() + 1);
    }

    @Override
    public @Nullable User findByUserId(int userId) {
        return db.get(userId);
    }

    @Override
    public int createUserAutoId(@NotNull User user) {
        for (int i = 0; i < 5; i++) {
            user.resetIdToAuto();
            int userId = generator.nextId();
            user.setIfAutoIdOrDie(userId);
            if (tryInsert(userId, user)) {
                return userId;
            }
        }
        throw new RuntimeException("Too many failed attempts to create a user");
    }

    private boolean tryInsert(int userId, @NotNull User user) {
        return db.putIfAbsent(userId, user) == null;
    }
}
