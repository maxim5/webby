package io.webby.auth.user;

import io.webby.db.model.LongAutoIdModel;
import org.jetbrains.annotations.NotNull;

public interface User extends LongAutoIdModel {
    String DB_NAME = "user";

    long userId();

    default boolean isAutoId() {
        return userId() == AUTO_ID;
    }

    @NotNull UserAccess access();
}
