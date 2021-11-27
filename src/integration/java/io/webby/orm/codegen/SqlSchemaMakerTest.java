package io.webby.orm.codegen;

import io.webby.auth.session.SessionTable;
import io.webby.auth.user.UserTable;
import io.webby.db.model.BlobKvTable;
import io.webby.orm.api.Engine;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static io.webby.orm.codegen.SqlSchemaMaker.makeCreateTableQuery;

public class SqlSchemaMakerTest {
    @Test
    public void create_table_user() {
        assertThat(makeCreateTableQuery(Engine.SQLite, UserTable.class)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS user (
                user_id INTEGER PRIMARY KEY,
                access_level INTEGER
            )
            """);

        assertThat(makeCreateTableQuery(Engine.H2, UserTable.class)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS user (
                user_id BIGINT PRIMARY KEY,
                access_level INTEGER
            )
            """);
    }

    @Test
    public void create_table_session() {
        assertThat(makeCreateTableQuery(Engine.SQLite, SessionTable.class)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS session (
                session_id INTEGER PRIMARY KEY,
                user_id INTEGER,
                created INTEGER,
                user_agent VARCHAR,
                ip_address VARCHAR
            )
            """);

        assertThat(makeCreateTableQuery(Engine.H2, SessionTable.class)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS session (
                session_id BIGINT PRIMARY KEY,
                user_id BIGINT,
                created TIMESTAMP,
                user_agent VARCHAR,
                ip_address VARCHAR
            )
            """);
    }

    @Test
    public void create_table_blob_kv() {
        assertThat(makeCreateTableQuery(Engine.SQLite, BlobKvTable.class)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS blob_kv (
                id VARCHAR PRIMARY KEY,
                value BLOB
            )
            """);

        assertThat(makeCreateTableQuery(Engine.H2, BlobKvTable.class)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS blob_kv (
                id VARCHAR PRIMARY KEY,
                value BLOB
            )
            """);
    }
}
