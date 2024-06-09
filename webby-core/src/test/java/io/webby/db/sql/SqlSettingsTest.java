package io.webby.db.sql;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.CheckReturnValue;
import io.webby.orm.api.Engine;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static com.google.common.truth.Truth.assertThat;
import static io.webby.db.sql.SqlSettings.parseEngineFromUrl;
import static io.webby.db.sql.SqlSettings.parseJdbcUrl;

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
        assertThat(parseEngineFromUrl("jdbc:mariadb://localhost:63249/test")).isEqualTo(Engine.MariaDB);
    }

    @Test
    public void parseEngineFromUrl_invalid() {
        assertThat(parseEngineFromUrl(null)).isEqualTo(Engine.Unknown);
        assertThat(parseEngineFromUrl("")).isEqualTo(Engine.Unknown);
        assertThat(parseEngineFromUrl("jdbc")).isEqualTo(Engine.Unknown);
        assertThat(parseEngineFromUrl("jdbc:")).isEqualTo(Engine.Unknown);
    }

    @Test
    public void parseJdbcUrl_valid() {
        assertUriThat(parseJdbcUrl("jdbc:derby:target/tmp/derby/hpjp;databaseName=foo;create=true"))
            .hasSchema("derby")
            .hasHost(null)
            .hasPort(-1)
            .hasPath(null);

        assertUriThat(parseJdbcUrl("jdbc:h2:file:/path/to/file.h2"))
            .hasSchema("h2")
            .hasHost(null)
            .hasPort(-1)
            .hasPath(null);

        assertUriThat(parseJdbcUrl("jdbc:sqlserver://localhost/"))
            .hasSchema("sqlserver")
            .hasHost("localhost")
            .hasPort(-1)
            .hasPath("/");

        assertUriThat(parseJdbcUrl("jdbc:mysql://localhost:0/foo"))
            .hasSchema("mysql")
            .hasHost("localhost")
            .hasPort(0)
            .hasPath("/foo");

        assertUriThat(parseJdbcUrl("jdbc:mariadb://localhost:63249/test"))
            .hasSchema("mariadb")
            .hasHost("localhost")
            .hasPort(63249)
            .hasPath("/test");
    }

    @Test
    public void parseJdbcUrl_invalid() {
        assertUriThat(parseJdbcUrl(null)).isNull();
        assertUriThat(parseJdbcUrl("")).isNull();
        assertUriThat(parseJdbcUrl("jdbc")).isNull();
    }

    @CheckReturnValue
    private static @NotNull UriSubject assertUriThat(@Nullable URI uri) {
        return new UriSubject(uri);
    }

    @CanIgnoreReturnValue
    private record UriSubject(@Nullable URI uri) {
        public @NotNull UriSubject isNull() {
            assertThat(uri).isNull();
            return this;
        }

        public @NotNull UriSubject hasSchema(@Nullable String schema) {
            assertThat(uri).isNotNull();
            assertThat(uri.getScheme()).isEqualTo(schema);
            return this;
        }

        public @NotNull UriSubject hasHost(@Nullable String host) {
            assertThat(uri).isNotNull();
            assertThat(uri.getHost()).isEqualTo(host);
            return this;
        }

        public @NotNull UriSubject hasPort(int port) {
            assertThat(uri).isNotNull();
            assertThat(uri.getPort()).isEqualTo(port);
            return this;
        }

        public @NotNull UriSubject hasPath(@Nullable String path) {
            assertThat(uri).isNotNull();
            assertThat(uri.getPath()).isEqualTo(path);
            return this;
        }
    }
}
