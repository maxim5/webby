package io.webby.auth.session;

import io.webby.testing.PrimaryKeyTableTest;
import io.webby.testing.SqliteTableTest;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static io.webby.testing.TestingUtil.array;

public class SessionTableTest
        extends SqliteTableTest<Long, Session, SessionTable>
        implements PrimaryKeyTableTest<Long, Session, SessionTable> {
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
    public @NotNull Long[] keys() {
        return array(1L, 2L);
    }

    @Override
    public @NotNull Session createEntity(@NotNull Long key, int version) {
        Instant created = Instant.now().truncatedTo(ChronoUnit.MILLIS);
        return new Session(key, 1, created, String.valueOf(version), version == 0 ? "127.0.0.1" : null);
    }
}
