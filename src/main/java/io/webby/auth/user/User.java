package io.webby.auth.user;

import io.webby.db.model.IntAutoIdModel;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;

public interface User extends IntAutoIdModel {
    String DB_NAME = "user";

    int userId();

    default boolean isAutoId() {
        return userId() == AUTO_ID;
    }

    @NotNull Instant created();

    @NotNull UserAccess access();
}
