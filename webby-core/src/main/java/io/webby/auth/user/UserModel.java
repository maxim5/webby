package io.webby.auth.user;

import io.webby.db.model.Ids;
import io.webby.db.model.IntAutoIdModel;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;

public interface UserModel extends IntAutoIdModel {
    String DB_NAME = "user";
    int NO_USER_ID = Ids.FOREIGN_ENTITY_NOT_EXISTS_INT;

    int userId();

    default boolean isAutoId() {
        return userId() == AUTO_ID;
    }

    @NotNull Instant createdAt();

    @NotNull UserAccess access();
}
