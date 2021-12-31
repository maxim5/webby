package io.webby.db.sql;

import io.webby.orm.api.Engine;
import org.junit.jupiter.api.Test;

import static io.webby.db.sql.SqlSettings.parseEngineFromUrl;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SqlSettingsTest {
    @Test
    public void parseEngineFromUrl_valid() {
        assertEquals(Engine.Derby, parseEngineFromUrl("jdbc:derby:target/tmp/derby/hpjp;databaseName=foo;create=true"));
        assertEquals(Engine.H2, parseEngineFromUrl("jdbc:h2:file:/path/to/file.h2"));
        assertEquals(Engine.H2, parseEngineFromUrl("jdbc:h2:mem:foo"));
        assertEquals(Engine.HyperSQL, parseEngineFromUrl("jdbc:hsqldb:mem:foo"));
        assertEquals(Engine.MsSqlServer, parseEngineFromUrl("jdbc:sqlserver://localhost;instance=SQLEXPRESS;databaseName=x"));
        assertEquals(Engine.MySQL, parseEngineFromUrl("jdbc:mysql://localhost/foo"));
        assertEquals(Engine.Oracle, parseEngineFromUrl("jdbc:oracle:thin:@localhost:1521:orclpdb1"));
        assertEquals(Engine.PostgreSQL, parseEngineFromUrl("jdbc:postgresql://localhost/foo"));
        assertEquals(Engine.SQLite, parseEngineFromUrl("jdbc:sqlite::memory:"));
    }

    @Test
    public void parseEngineFromUrl_invalid() {
        assertEquals(Engine.Unknown, parseEngineFromUrl(null));
        assertEquals(Engine.Unknown, parseEngineFromUrl(""));
        assertEquals(Engine.Unknown, parseEngineFromUrl("jdbc"));
        assertEquals(Engine.Unknown, parseEngineFromUrl("jdbc:"));
    }
}
