package io.webby.auth.session;

import com.google.inject.Inject;
import io.webby.db.model.LongAutoIdModel;
import io.webby.db.sql.TableManager;
import io.webby.netty.request.HttpRequestEx;
import io.webby.orm.api.TableLong;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static io.webby.util.base.EasyCast.castAny;

public class SqlSessionStore implements SessionStore {
    protected final TableLong<SessionModel> table;

    @Inject
    public SqlSessionStore(@NotNull TableManager manager, @NotNull Class<? extends SessionModel> sessionClass) {
        table = castAny(manager.getMatchingTableOrDie(SessionModel.DB_NAME, Long.class, sessionClass));
    }

    @Override
    public int size() {
        return table.count();
    }

    @Override
    public @Nullable SessionModel getSessionByIdOrNull(long sessionId) {
        return table.getByPkOrNull(sessionId);
    }

    @Override
    public @NotNull SessionModel createSessionAutoId(@NotNull HttpRequestEx request) {
        SessionModel session = DefaultSession.fromRequest(LongAutoIdModel.AUTO_ID, request);
        long autoId = table.insertAutoIncPk(session);
        return session.withSessionId(autoId);
    }

    @Override
    public void updateSessionById(@NotNull SessionModel session) {
        table.updateByPk(session);
    }
}
