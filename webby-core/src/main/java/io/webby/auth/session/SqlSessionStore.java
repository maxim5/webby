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
    protected final TableLong<Session> table;

    @Inject
    public SqlSessionStore(@NotNull TableManager manager) {
        table = castAny(manager.getMatchingTableOrDie(Session.DB_NAME, Long.class, Session.class));
    }

    @Override
    public int size() {
        return table.count();
    }

    @Override
    public @Nullable Session getSessionByIdOrNull(long sessionId) {
        return table.getByPkOrNull(sessionId);
    }

    @Override
    public @NotNull Session createSessionAutoId(@NotNull HttpRequestEx request) {
        Session session = Session.fromRequest(LongAutoIdModel.AUTO_ID, request);
        long autoId = table.insertAutoIncPk(session);
        return session.withSessionId(autoId);
    }

    @Override
    public void updateSessionById(@NotNull Session session) {
        table.updateByPk(session);
    }
}
