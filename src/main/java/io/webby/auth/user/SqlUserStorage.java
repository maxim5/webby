package io.webby.auth.user;

import com.google.inject.Inject;
import io.webby.db.sql.TableManager;
import io.webby.orm.api.TableInt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static io.webby.util.base.EasyCast.castAny;

public class SqlUserStorage implements UserStorage {
    private final TableInt<UserModel> table;

    @Inject
    public SqlUserStorage(@NotNull TableManager manager, @NotNull Class<? extends UserModel> userClass) {
        table = castAny(manager.getMatchingTableOrDie(UserModel.DB_NAME, Integer.class, userClass));
    }

    @Override
    public @Nullable UserModel findByUserId(int userId) {
        return table.getByPkOrNull(userId);
    }

    @Override
    public int createUserAutoId(@NotNull UserModel user) {
        int autoId = table.insertAutoIncPk(user);
        user.setIfAutoIdOrDie(autoId);
        return autoId;
    }
}
