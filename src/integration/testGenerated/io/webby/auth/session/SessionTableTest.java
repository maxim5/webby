package io.webby.auth.session;

import io.webby.testing.BaseModelKeyTableTest;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static io.webby.testing.TestingUtil.array;

public class SessionTableTest extends BaseModelKeyTableTest<Long, Session, SessionTable> {
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
        keys = array(1L, 2L);
        table = new SessionTable(connection);
    }

    @Override
    protected @NotNull Session createEntity(@NotNull Long key, int version) {
        Instant created = Instant.now().truncatedTo(ChronoUnit.MILLIS);
        return new Session(key, 1, created, String.valueOf(version), version == 0 ? "127.0.0.1" : null);
    }
}
