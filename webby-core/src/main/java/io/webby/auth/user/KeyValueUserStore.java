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

public class KeyValueUserStore implements UserStore {
    private final KeyValueDb<Integer, UserModel> db;
    private final IntIdGenerator generator;

    @Inject
    public KeyValueUserStore(@NotNull Settings settings,
                             @NotNull Class<? extends UserModel> userClass,
                             @NotNull KeyValueFactory dbFactory) {
        boolean randomIds = settings.getBoolProperty("user.id.generator.random.enabled");
        db = castAny(dbFactory.getDb(DbOptions.of(UserModel.DB_NAME, Integer.class, userClass)));
        generator = randomIds ? IntIdGenerator.random(null) : IntIdGenerator.autoIncrement(() -> db.size() + 1);
    }

    @Override
    public int size() {
        return db.size();
    }

    @Override
    public @NotNull Iterable<? extends UserModel> fetchAllUsers() {
        return db.values();
    }

    @Override
    public @Nullable UserModel findByUserId(int userId) {
        return db.get(userId);
    }

    @Override
    public int createUserAutoId(@NotNull UserModel user) {
        assert user.isAutoId() : "User is not auto-id: %s".formatted(user);
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

    private boolean tryInsert(int userId, @NotNull UserModel user) {
        return db.putIfAbsent(userId, user) == null;
    }
}
