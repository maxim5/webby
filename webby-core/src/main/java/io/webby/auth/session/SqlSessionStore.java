package io.webby.auth.session;

import com.google.inject.Inject;
import io.spbx.orm.api.TableLong;
import io.webby.db.sql.TableManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static io.spbx.util.base.EasyCast.castAny;

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
    public @NotNull SessionModel createSessionAutoId(@NotNull SessionData data) {
        SessionModel entity = data instanceof SessionModel model ? model : data.toSessionModel(SessionModel.AUTO_ID);
        assert entity.isAutoId() : "Session is not auto-id: %s".formatted(data);
        long autoId = table.insertAutoIncPk(entity);
        return entity.toSessionModel(autoId);
    }

    @Override
    public void updateSessionById(@NotNull SessionModel session) {
        table.updateByPkOrInsert(session);
    }
}
