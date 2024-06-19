package io.spbx.webby.auth.session;

import com.google.inject.Inject;
import io.spbx.util.base.Pair;
import io.spbx.webby.app.Settings;
import io.spbx.webby.db.kv.DbOptions;
import io.spbx.webby.db.kv.KeyValueAutoRetryInserter;
import io.spbx.webby.db.kv.KeyValueDb;
import io.spbx.webby.db.kv.KeyValueFactory;
import io.spbx.webby.db.model.LongIdGenerator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.security.SecureRandom;

import static io.spbx.util.base.EasyCast.castAny;

public class KeyValueSessionStore implements SessionStore {
    protected final KeyValueDb<Long, SessionModel> db;
    protected final KeyValueAutoRetryInserter<Long, SessionModel> inserter;

    @Inject
    public KeyValueSessionStore(@NotNull Settings settings,
                                @NotNull Class<? extends SessionModel> sessionClass,
                                @NotNull KeyValueFactory factory) throws Exception {
        boolean randomIds = settings.getBoolProperty("session.id.generator.random.enabled", true);
        int maxAttempts = settings.getIntProperty("session.id.generator.max.attempts", 5);
        db = castAny(factory.getDb(DbOptions.of(DefaultSession.DB_NAME, Long.class, sessionClass)));
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
    public @Nullable SessionModel getSessionByIdOrNull(long sessionId) {
        return db.get(sessionId);
    }

    @Override
    public @NotNull SessionModel createSessionAutoId(@NotNull SessionData data) {
        assert !(data instanceof SessionModel model) || model.isAutoId() : "Session is not auto-id: %s".formatted(data);
        Pair<Long, SessionModel> inserted = inserter.insertOrDie(data::toSessionModel);
        return inserted.second();
    }

    @Override
    public void updateSessionById(@NotNull SessionModel session) {
        db.set(session.sessionId(), session);
    }
}
