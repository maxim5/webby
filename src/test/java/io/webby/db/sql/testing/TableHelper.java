package io.webby.db.sql.testing;

public class TableHelper {
    public static final String CREATE_USER_TABLE_SQL = """
        CREATE TABLE IF NOT EXISTS user (
            user_id INTEGER PRIMARY KEY,
            access_level INTEGER
        )
        """;

    public static final String CREATE_SESSION_TABLE_SQL = """
        CREATE TABLE IF NOT EXISTS session (
            session_id INTEGER PRIMARY KEY,
            user_id INTEGER,
            created INTEGER,
            user_agent TEXT,
            ip_address TEXT
        )
        """;

    public static final String CREATE_BLOB_KV_TABLE_SQL = """
        CREATE TABLE IF NOT EXISTS blob_kv (
            id BLOB PRIMARY KEY,
            value BLOB
        )
        """;
}
