package io.webby.auth.session;

import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

public record Session(long sessionId, @NotNull Instant created) {
    private static final long JUST_CREATED_MILLIS = TimeUnit.SECONDS.toMillis(60);
    private static final long TIME_TO_REFRESH_MILLIS = TimeUnit.DAYS.toMillis(30);

    public boolean shouldRefresh() {
        long createdMillis = created.getEpochSecond() * 1000;
        long now = System.currentTimeMillis();
        return createdMillis + JUST_CREATED_MILLIS >= now || createdMillis + TIME_TO_REFRESH_MILLIS < now;
    }
}
