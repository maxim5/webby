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
import java.time.temporal.ChronoUnit;

public class SessionTableTest
        extends SqlDbTableTest<Session, SessionTable>
        implements TableLongTest<Session, SessionTable> {
    @Override
    protected void setUp(@NotNull Connector connector) throws Exception {
        table = new SessionTable(connector);
        table.admin().createTable(CreateTableQuery.of(table).ifNotExists());
        table.admin().createTable(CreateTableQuery.of(UserTable.META).ifNotExists());
    }

    @Override
    protected void fillUp(@NotNull Connector connector) {
        // These contents support any entity version (below).
        new UserTable(connector).insert(new DefaultUser(1, Instant.now(), UserAccess.Simple));
    }

    @Override
    public @NotNull Session createEntity(@NotNull Long key, int version) {
        Instant created = Instant.now().truncatedTo(ChronoUnit.MILLIS);
        return new Session(key, ForeignInt.ofId(1), created, String.valueOf(version), version == 0 ? "127.0.0.1" : null);
    }

    @Override
    public @NotNull Session copyEntityWithId(@NotNull Session session, long autoId) {
        return new Session(autoId, session.user(), session.createdAt(), session.userAgent(), session.ipAddress());
    }
}
