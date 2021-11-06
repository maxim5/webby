package io.webby.auth.session;

import io.webby.testing.BaseModelKeyTableTest;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class SessionTableTest extends BaseModelKeyTableTest<Long, Session, SessionTable> {
    @Override
    protected void setUp(@NotNull Connection connection) throws Exception {
        connection.createStatement().executeUpdate("""
        CREATE TABLE session (session_id INTEGER PRIMARY KEY,
                              user_id INTEGER,
                              created INTEGER,
                              user_agent TEXT,
                              ip_address TEXT)
        """);
        key1 = 1L;
        key2 = 2L;
        table = new SessionTable(connection);
    }

    @Override
    protected @NotNull Session createEntity(@NotNull Long key, int version) {
        return new Session(key, 1, Instant.now().truncatedTo(ChronoUnit.MILLIS), String.valueOf(version), "127.0.0.1");
    }
}
