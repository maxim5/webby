package io.webby.auth.user;

import com.google.inject.Inject;
import io.webby.db.sql.TableManager;
import io.webby.orm.api.TableInt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static io.spbx.util.base.EasyCast.castAny;

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
    public @NotNull UserModel createUserAutoId(@NotNull UserData data) {
        UserModel entity = data instanceof UserModel userModel ? userModel : data.toUserModel(UserModel.AUTO_ID);
        assert entity.isAutoId() : "User is not auto-id: %s".formatted(data);
        int autoId = table.insertAutoIncPk(entity);
        return data.toUserModel(autoId);
    }
}
