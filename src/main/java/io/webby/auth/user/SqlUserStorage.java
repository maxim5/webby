package io.webby.auth.user;

import com.google.inject.Inject;
import io.webby.app.Settings;
import io.webby.db.sql.TableManager;
import io.webby.orm.api.TableInt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static io.webby.util.base.EasyCast.castAny;

public class SqlUserStorage implements UserStorage {
    private final TableInt<User> table;

    @Inject
    public SqlUserStorage(@NotNull Settings settings, @NotNull TableManager manager) {
        Class<? extends User> userClass = DefaultUser.class;
        table = castAny(manager.getMatchingTableOrDie(User.DB_NAME, Integer.class, userClass));
    }

    @Override
    public @Nullable User findByUserId(int userId) {
        return table.getByPkOrNull(userId);
    }

    @Override
    public int createUserAutoId(@NotNull User user) {
        int autoId = table.insertAutoIncPk(user);
        user.setIfAutoIdOrDie(autoId);
        return autoId;
    }
}
