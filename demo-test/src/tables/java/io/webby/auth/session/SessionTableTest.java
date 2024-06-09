package io.webby.auth.session;

import io.webby.auth.user.DefaultUser;
import io.webby.auth.user.UserAccess;
import io.webby.auth.user.UserTable;
import io.webby.orm.api.Connector;
import io.webby.orm.api.ForeignInt;
import io.webby.orm.api.query.CreateTableQuery;
import io.webby.testing.SqlDbTableTest;
import io.webby.testing.TableLongTest;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;

public class SessionTableTest
        extends SqlDbTableTest<DefaultSession, SessionTable>
        implements TableLongTest<DefaultSession, SessionTable> {
    @Override
    protected void setUp(@NotNull Connector connector) throws Exception {
        table = new SessionTable(connector);
        table.admin().createTable(CreateTableQuery.of(UserTable.META).ifNotExists());
        table.admin().createTable(CreateTableQuery.of(table).ifNotExists());
    }

    @Override
    protected void fillUp(@NotNull Connector connector) {
        // These contents support any entity version (below).
        new UserTable(connector).insert(DefaultUser.newUser(1, Instant.now(), UserAccess.Simple));
    }

    @Override
    public @NotNull DefaultSession createEntity(@NotNull Long key, int version) {
        String ipAddress = version == 0 ? "127.0.0.1" : null;
        return DefaultSession.newSession(key, ForeignInt.ofId(1), Instant.now(), String.valueOf(version), ipAddress);
    }

    @Override
    public @NotNull DefaultSession copyEntityWithId(@NotNull DefaultSession session, long autoId) {
        return DefaultSession.newSession(autoId, session.user(), session.createdAt(), session.userAgent(), session.ipAddress());
    }
}
