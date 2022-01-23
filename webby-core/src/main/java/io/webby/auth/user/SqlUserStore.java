package io.webby.auth.user;

import com.google.inject.Inject;
import io.webby.db.sql.TableManager;
import io.webby.orm.api.TableInt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static io.webby.util.base.EasyCast.castAny;

public class SqlUserStore implements UserStore {
    protected final TableInt<UserModel> table;

    @Inject
    public SqlUserStore(@NotNull TableManager manager, @NotNull Class<? extends UserModel> userClass) {
        table = castAny(manager.getMatchingTableOrDie(UserModel.DB_NAME, Integer.class, userClass));
    }

    @Override
    public int size() {
        return table.count();
    }

    @Override
    public @NotNull Iterable<? extends UserModel> fetchAllUsers() {
        return table.fetchAll();
    }

    @Override
    public @Nullable UserModel getUserByIdOrNull(int userId) {
        return table.getByPkOrNull(userId);
    }

    @Override
    public int createUserAutoId(@NotNull UserModel user) {
        assert user.isAutoId() : "User is not auto-id: %s".formatted(user);
        int autoId = table.insertAutoIncPk(user);
        user.setIfAutoIdOrDie(autoId);
        return autoId;
    }
}
