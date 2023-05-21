package io.webby.db.sql;

import io.webby.orm.api.Engine;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static io.webby.db.sql.SqlSettings.parseEngineFromUrl;

public class SqlSettingsTest {
    @Test
    public void parseEngineFromUrl_valid() {
        assertThat(parseEngineFromUrl("jdbc:derby:target/tmp/derby/hpjp;databaseName=foo;create=true")).isEqualTo(Engine.Derby);
        assertThat(parseEngineFromUrl("jdbc:h2:file:/path/to/file.h2")).isEqualTo(Engine.H2);
        assertThat(parseEngineFromUrl("jdbc:h2:mem:foo")).isEqualTo(Engine.H2);
        assertThat(parseEngineFromUrl("jdbc:hsqldb:mem:foo")).isEqualTo(Engine.HyperSQL);
        assertThat(parseEngineFromUrl("jdbc:sqlserver://localhost;instance=SQLEXPRESS;databaseName=x")).isEqualTo(Engine.MsSqlServer);
        assertThat(parseEngineFromUrl("jdbc:mysql://localhost/foo")).isEqualTo(Engine.MySQL);
        assertThat(parseEngineFromUrl("jdbc:oracle:thin:@localhost:1521:orclpdb1")).isEqualTo(Engine.Oracle);
        assertThat(parseEngineFromUrl("jdbc:postgresql://localhost/foo")).isEqualTo(Engine.PostgreSQL);
        assertThat(parseEngineFromUrl("jdbc:sqlite::memory:")).isEqualTo(Engine.SQLite);
    }

    @Test
    public void parseEngineFromUrl_invalid() {
        assertThat(parseEngineFromUrl(null)).isEqualTo(Engine.Unknown);
        assertThat(parseEngineFromUrl("")).isEqualTo(Engine.Unknown);
        assertThat(parseEngineFromUrl("jdbc")).isEqualTo(Engine.Unknown);
        assertThat(parseEngineFromUrl("jdbc:")).isEqualTo(Engine.Unknown);
    }
}
