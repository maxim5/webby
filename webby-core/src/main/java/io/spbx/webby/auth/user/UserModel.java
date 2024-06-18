package io.spbx.webby.auth.user;

import io.spbx.orm.api.ForeignInt;
import io.spbx.webby.db.model.IntAutoIdModel;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;

public interface UserModel extends UserData, IntAutoIdModel {
    String DB_NAME = "user";

    int userId();

    @Override
    default boolean isAutoId() {
        return userId() == AUTO_ID;
    }

    @NotNull Instant createdAt();

    @NotNull UserAccess access();

    default @NotNull ForeignInt<UserModel> toForeignInt() {
        return ForeignInt.ofEntity(userId(), this);
    }
}
