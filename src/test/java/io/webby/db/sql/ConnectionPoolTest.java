package io.webby.db.sql;

import io.webby.orm.api.Engine;
import org.junit.jupiter.api.Test;

import static io.webby.db.sql.ConnectionPool.parseUrl;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConnectionPoolTest {
    @Test
    public void parseUrl_valid() {
        assertEquals(Engine.Derby, parseUrl("jdbc:derby:target/tmp/derby/hpjp;databaseName=foo;create=true"));
        assertEquals(Engine.H2, parseUrl("jdbc:h2:file:/path/to/file.h2"));
        assertEquals(Engine.H2, parseUrl("jdbc:h2:mem:foo"));
        assertEquals(Engine.HyperSQL, parseUrl("jdbc:hsqldb:mem:foo"));
        assertEquals(Engine.MsSqlServer, parseUrl("jdbc:sqlserver://localhost;instance=SQLEXPRESS;databaseName=foo"));
        assertEquals(Engine.MySQL, parseUrl("jdbc:mysql://localhost/foo"));
        assertEquals(Engine.Oracle, parseUrl("jdbc:oracle:thin:@localhost:1521:orclpdb1"));
        assertEquals(Engine.PostgreSQL, parseUrl("jdbc:postgresql://localhost/foo"));
        assertEquals(Engine.SQLite, parseUrl("jdbc:sqlite::memory:"));
    }

    @Test
    public void parseUrl_invalid() {
        assertEquals(Engine.Unknown, parseUrl(null));
        assertEquals(Engine.Unknown, parseUrl(""));
        assertEquals(Engine.Unknown, parseUrl("jdbc"));
        assertEquals(Engine.Unknown, parseUrl("jdbc:"));
    }
}
