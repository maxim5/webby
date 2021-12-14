package io.webby.auth.session;

import io.webby.orm.api.Connector;
import io.webby.orm.codegen.SqlSchemaMaker;
import io.webby.testing.SqlDbTableTest;
import io.webby.testing.TableLongTest;
import io.webby.orm.api.ForeignLong;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class SessionTableTest
        extends SqlDbTableTest<Long, Session, SessionTable>
        implements TableLongTest<Session, SessionTable> {
    @Override
    protected void setUp(@NotNull Connector connector) throws Exception {
        table = new SessionTable(connector);
        connector().runner().runUpdate(SqlSchemaMaker.makeCreateTableQuery(table));
    }

    @Override
    public @NotNull Session createEntity(@NotNull Long key, int version) {
        Instant created = Instant.now().truncatedTo(ChronoUnit.MILLIS);
        return new Session(key, ForeignLong.ofId(1), created, String.valueOf(version), version == 0 ? "127.0.0.1" : null);
    }

    @Override
    public @NotNull Session copyEntityWithId(@NotNull Session session, long autoId) {
        return new Session(autoId, session.user(), session.created(), session.userAgent(), session.ipAddress());
    }
}
