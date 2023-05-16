package io.webby.auth.user;

import io.webby.db.model.IntAutoIdModel;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;

public interface UserModel extends IntAutoIdModel {
    String DB_NAME = "user";

    int userId();

    @Override
    default boolean isAutoId() {
        return userId() == AUTO_ID;
    }

    @NotNull Instant createdAt();

    @NotNull UserAccess access();
}
