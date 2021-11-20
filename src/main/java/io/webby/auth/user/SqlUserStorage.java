package io.webby.auth.user;

import com.google.inject.Inject;
import io.webby.app.Settings;
import io.webby.db.sql.TableManager;
import io.webby.util.sql.api.TableLong;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static io.webby.util.base.EasyCast.castAny;

public class SqlUserStorage implements UserStorage {
    private final TableLong<User> table;

    @Inject
    public SqlUserStorage(@NotNull Settings settings, @NotNull TableManager manager) {
        Class<? extends User> userClass = DefaultUser.class;
        table = castAny(manager.getMatchingTableOrDie(User.DB_NAME, Long.class, userClass));
    }

    @Override
    public @Nullable User findByUserId(long userId) {
        return table.getByPkOrNull(userId);
    }

    @Override
    public long createUserAutoId(@NotNull User user) {
        long autoId = table.insertAutoIncPk(user);
        user.setIfAutoIdOrDie(autoId);
        return autoId;
    }
}
