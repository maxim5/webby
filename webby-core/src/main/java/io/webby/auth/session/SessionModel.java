package io.webby.auth.session;

import io.webby.auth.user.UserModel;
import io.webby.db.model.LongAutoIdModel;
import io.webby.orm.api.ForeignInt;
import io.webby.orm.api.ForeignLong;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;

public interface SessionModel extends SessionData, LongAutoIdModel {
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

    default boolean hasUserId() {
        return user().isPresent();
    }

    default boolean isAuthenticated() {
        return hasUserId();
    }

    @NotNull Instant createdAt();

    @NotNull String userAgent();

    @Nullable String ipAddress();

    @NotNull SessionModel withUser(@NotNull UserModel user);

    default @NotNull ForeignLong<SessionModel> toForeignLong() {
        return ForeignLong.ofEntity(sessionId(), this);
    }
}
