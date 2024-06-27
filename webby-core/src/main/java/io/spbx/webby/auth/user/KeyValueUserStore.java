package io.spbx.webby.auth.user;

import com.google.inject.Inject;
import io.spbx.util.base.Pair;
import io.spbx.util.props.PropertyMap;
import io.spbx.webby.db.kv.DbOptions;
import io.spbx.webby.db.kv.KeyValueAutoRetryInserter;
import io.spbx.webby.db.kv.KeyValueDb;
import io.spbx.webby.db.kv.KeyValueFactory;
import io.spbx.webby.db.model.IntIdGenerator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static io.spbx.util.base.EasyCast.castAny;

public class KeyValueUserStore implements UserStore {
    protected final KeyValueDb<Integer, UserModel> db;
    protected final KeyValueAutoRetryInserter<Integer, UserModel> inserter;

    @Inject
    public KeyValueUserStore(@NotNull PropertyMap properties,
                             @NotNull Class<? extends UserModel> userClass,
                             @NotNull KeyValueFactory dbFactory) {
        boolean randomIds = properties.getBool("user.id.generator.random.enabled", true);
        int maxAttempts = properties.getInt("user.id.generator.max.attempts", 5);
        db = castAny(dbFactory.getDb(DbOptions.of(UserModel.DB_NAME, Integer.class, userClass)));
        IntIdGenerator generator = randomIds ?
            IntIdGenerator.positiveRandom(null) :
            IntIdGenerator.autoIncrement(() -> db.size() + 1);
        inserter = new KeyValueAutoRetryInserter<>(db, generator, maxAttempts);
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
    public @Nullable UserModel getUserByIdOrNull(int userId) {
        return db.get(userId);
    }

    @Override
    public @NotNull UserModel createUserAutoId(@NotNull UserData data) {
        assert !(data instanceof UserModel userModel) || userModel.isAutoId() : "User is not auto-id: %s".formatted(data);
        Pair<Integer, UserModel> inserted = inserter.insertOrDie(data::toUserModel);
        return inserted.second();
    }
}
