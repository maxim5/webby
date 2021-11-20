package io.webby.auth.session;

import io.webby.testing.SqliteTableTest;
import io.webby.testing.TableLongTest;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class SessionTableTest
        extends SqliteTableTest<Long, Session, SessionTable>
        implements TableLongTest<Session, SessionTable> {
    @Override
    protected void setUp(@NotNull Connection connection) throws Exception {
        connection.createStatement().executeUpdate("""
            CREATE TABLE session (
                session_id INTEGER PRIMARY KEY,
                user_id INTEGER,
                created INTEGER,
                user_agent TEXT,
                ip_address TEXT
            )
        """);
        table = new SessionTable(connection);
    }

    @Override
    public @NotNull Session createEntity(@NotNull Long key, int version) {
        Instant created = Instant.now().truncatedTo(ChronoUnit.MILLIS);
        return new Session(key, 1, created, String.valueOf(version), version == 0 ? "127.0.0.1" : null);
    }

    @Override
    public @NotNull Session copyEntityWithId(@NotNull Session session, long autoId) {
        return new Session(autoId, session.userId(), session.created(), session.userAgent(), session.ipAddress());
    }
}
