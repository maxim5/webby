package io.webby.auth.session;

import io.webby.auth.user.UserModel;
import io.webby.db.model.LongAutoIdModel;
import io.webby.orm.api.ForeignInt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;

public interface SessionModel extends LongAutoIdModel {
    String DB_NAME = "session";

    long sessionId();

    @Override
    default boolean isAutoId() {
        return sessionId() == AUTO_ID;
    }

    @NotNull ForeignInt<UserModel> user();

    default int userId() {
        return user().getIntId();
    }

    default boolean hasUser() {
        return user().isPresent();
    }

    @NotNull Instant createdAt();

    @NotNull String userAgent();

    @Nullable String ipAddress();
}
