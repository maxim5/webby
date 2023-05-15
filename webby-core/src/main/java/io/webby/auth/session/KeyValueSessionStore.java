package io.webby.auth.session;

import com.google.inject.Inject;
import io.webby.app.Settings;
import io.webby.db.kv.DbOptions;
import io.webby.db.kv.KeyValueAutoRetryInserter;
import io.webby.db.kv.KeyValueDb;
import io.webby.db.kv.KeyValueFactory;
import io.webby.db.model.LongIdGenerator;
import io.webby.netty.request.HttpRequestEx;
import io.webby.util.collect.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.security.SecureRandom;

public class KeyValueSessionStore implements SessionStore {
    protected final KeyValueDb<Long, Session> db;
    protected final KeyValueAutoRetryInserter<Long, Session> inserter;

    @Inject
    public KeyValueSessionStore(@NotNull Settings settings, @NotNull KeyValueFactory factory) throws Exception {
        boolean randomIds = settings.getBoolProperty("session.id.generator.random.enabled", true);
        int maxAttempts = settings.getIntProperty("session.id.generator.max.attempts", 5);
        db = factory.getDb(DbOptions.of(Session.DB_NAME, Long.class, Session.class));
        LongIdGenerator generator = randomIds ?
            LongIdGenerator.securePositiveRandom(SecureRandom.getInstance("SHA1PRNG")) :
            LongIdGenerator.autoIncrement(() -> db.size() + 1);
        inserter = new KeyValueAutoRetryInserter<>(db, generator, maxAttempts);
    }

    @Override
    public int size() {
        return db.size();
    }

    @Override
    public @Nullable Session getSessionByIdOrNull(long sessionId) {
        return db.get(sessionId);
    }

    @Override
    public @NotNull Session createSessionAutoId(@NotNull HttpRequestEx request) {
        Pair<Long, Session> inserted = inserter.insertOrDie(sessionId -> Session.fromRequest(sessionId, request));
        return inserted.second();
    }

    @Override
    public void updateSessionById(@NotNull Session session) {
        db.set(session.sessionId(), session);
    }
}
