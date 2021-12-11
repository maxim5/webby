package io.webby.examples;

import com.google.common.io.BaseEncoding;
import io.webby.db.sql.SqlSettings;
import io.webby.orm.api.DebugSql;
import io.webby.orm.api.QueryRunner;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.*;
import java.time.Instant;

import static io.webby.db.sql.SqlSettings.*;

@SuppressWarnings("unused")
public class Playground {
    public static void main(String[] args) throws Exception {
        // runH2();
        // runSqlite();
        runMysql();
    }

    private static void runMysql() throws SQLException {
        try (Connection connection = connect(MYSQL_TEST)) {
            QueryRepl main = new QueryRepl(connection);

            main.update("DROP TABLE IF EXISTS userx");
            main.update("CREATE TABLE userx (user_id INTEGER PRIMARY KEY AUTO_INCREMENT, access_level INTEGER, txt VARCHAR(1024))");

            main.update("DROP TABLE IF EXISTS blobx");
            main.update("CREATE TABLE blobx (blob_id VARBINARY(64) PRIMARY KEY, blob_value BLOB)");
            main.update("INSERT INTO blobx(blob_id, blob_value) VALUES(?, ?)", "foo".getBytes(), "bar".getBytes());
            main.update("INSERT INTO blobx(blob_id, blob_value) VALUES(?, ?)", "for".getBytes(), "baz".getBytes());
            main.update("INSERT INTO blobx(blob_id, blob_value) VALUES(?, ?)", "x".getBytes(), "y".getBytes());

            main.query("SELECT * FROM blobx");
            main.query("SELECT * FROM blobx WHERE blob_id LIKE 'f%'");
            main.query("SELECT * FROM blobx WHERE blob_id LIKE 'foo%'");
            main.query("SELECT * FROM blobx WHERE blob_id = ?", "foo");
            main.query("SELECT * FROM blobx WHERE blob_id = CAST(? AS BINARY(64))", "foo");
            main.query("SELECT * FROM blobx WHERE blob_value = ?", "baz");
            main.query("SELECT * FROM blobx WHERE blob_value = ?", (Object) "baz".getBytes());

            main.update("DROP TABLE IF EXISTS timex");
            main.update("CREATE TABLE timex (id INTEGER PRIMARY KEY AUTO_INCREMENT, time TIMESTAMP(3))");
            main.update("INSERT INTO timex(time) VALUES(?)", /*System.currentTimeMillis()*/Timestamp.from(Instant.now()));
            main.query("SELECT * FROM timex");

            main.query("SHOW CREATE TABLE userx");
            main.query("SHOW COLUMNS FROM userx");
        }
    }

    private static void runSqlite() throws SQLException {
        try (Connection connection = connect(SQLITE_IN_MEMORY)) {
            QueryRepl main = new QueryRepl(connection);
            sqliteTextForBlobPk(main);
            // sqliteJoins(main);
            // sqliteBlobPk(main);
        }
    }

    private static void runH2() throws SQLException {
        try (Connection connection = connect(H2_IN_MEMORY)) {
            QueryRepl main = new QueryRepl(connection);

            main.update("CREATE TABLE blob (blob_id VARCHAR PRIMARY KEY, blob_value BLOB)");
            main.query("SELECT * FROM blob");
            main.query("SELECT COUNT(*) FROM blob WHERE blob_id LIKE 'foo:%'");

            main.update("INSERT INTO blob(blob_id, blob_value) VALUES(?, ?)", "foo".getBytes(), "bar".getBytes());
            main.update("INSERT INTO blob(blob_id, blob_value) VALUES(?, ?)", "for".getBytes(), "baz".getBytes());
            main.update("INSERT INTO blob(blob_id, blob_value) VALUES(?, ?)", "x".getBytes(), "y".getBytes());
            main.query("SELECT * FROM blob");

            main.query("SELECT * FROM blob WHERE blob_id LIKE '66%'");
            main.query("SELECT * FROM blob WHERE blob_id LIKE '666f6f%'");

            main.query("SELECT COUNT(*) FROM blob WHERE blob_id LIKE 'foo%'");
            main.query("SELECT COUNT(*) FROM blob WHERE blob_id LIKE '666F6F%'");

            main.query("SHOW COLUMNS FROM blob");
        }
    }

    private static void sqliteTextForBlobPk(QueryRepl main) throws SQLException {
        main.update("CREATE TABLE blob (blob_id VARCHAR PRIMARY KEY, blob_value BLOB)");
        main.update("INSERT INTO blob(blob_id, blob_value) VALUES(?, ?)", "foo".getBytes(), "bar".getBytes());
        main.update("INSERT INTO blob(blob_id, blob_value) VALUES(?, ?)", "for".getBytes(), "baz".getBytes());
        main.update("INSERT INTO blob(blob_id, blob_value) VALUES(?, ?)", "x".getBytes(), "y".getBytes());

        main.query("SELECT * FROM blob");
        main.query("SELECT blob_id, hex(blob_value) FROM blob");
        main.query("SELECT hex(blob_id), hex(blob_value) FROM blob");

        main.query("SELECT * FROM blob WHERE hex(blob_id) LIKE '66%'");
        main.query("SELECT * FROM blob WHERE hex(blob_id) LIKE '666f6f%'");

        main.query("SELECT sql FROM sqlite_master WHERE name=?", "blob");
    }

    private static void sqliteBlobPk(QueryRepl main) throws SQLException {
        main.update("CREATE TABLE blob (blob_id BLOB PRIMARY KEY, blob_value BLOB)");
        main.update("INSERT INTO blob(blob_id, blob_value) VALUES(?, ?)", "foo".getBytes(), "bar".getBytes());
        main.update("INSERT INTO blob(blob_id, blob_value) VALUES(?, ?)", "for".getBytes(), "baz".getBytes());
        main.update("INSERT INTO blob(blob_id, blob_value) VALUES(?, ?)", "x".getBytes(), "y".getBytes());
        main.query("SELECT * FROM blob");
        main.query("SELECT hex(blob_id), hex(blob_value) FROM blob");

        // https://stackoverflow.com/questions/8892973/how-to-get-last-insert-id-in-sqlite
        main.query("SELECT last_insert_rowid()");
        // main.query("SELECT LAST_INSERT_ID()");
        // main.query("SELECT SCOPE_IDENTITY()");

        // https://stackoverflow.com/questions/24011247/fast-search-on-a-blob-starting-bytes-in-sqlite
        // https://stackoverflow.com/questions/3746756/search-for-value-within-blob-column-in-mysql
        main.query("SELECT * FROM blob WHERE hex(blob_id) LIKE '66%'");
        main.query("SELECT * FROM blob WHERE hex(blob_id) LIKE '666f6f%'");

        System.out.println(BaseEncoding.base16().lowerCase().encode("foo".getBytes()));
    }

    private static void sqliteJoins(QueryRepl main) throws SQLException {
        main.update("CREATE TABLE user (user_id INTEGER PRIMARY KEY AUTOINCREMENT, access_level INTEGER)");
        main.update("CREATE TABLE song (id INTEGER PRIMARY KEY AUTOINCREMENT, author_id INTEGER, FOREIGN KEY(author_id) REFERENCES user(user_id))");
        main.update("CREATE TABLE single (id INTEGER PRIMARY KEY AUTOINCREMENT, song_id INTEGER, FOREIGN KEY(song_id) REFERENCES song(song_id))");

        main.update("INSERT INTO user(user_id, access_level) VALUES(?, ?)", null, 111);
        main.update("INSERT INTO user(user_id, access_level) VALUES(?, ?)", 0, 222);
        main.update("INSERT INTO song(id, author_id) VALUES(?, ?)", null, 1);
        main.update("INSERT INTO single(id, song_id) VALUES(?, ?)", null, 1);

        main.query("SELECT * from user");
        main.query("SELECT * from song");
        main.query("SELECT * from single");

        main.query("""
            SELECT single.id FROM single
            WHERE id=1;
        """);

        main.query("""
            SELECT song.id, user.user_id, user.access_level FROM song
            LEFT JOIN user ON song.author_id = user.user_id;
        """);

        main.query("""
            SELECT single.id, song.id, user.user_id, user.access_level FROM single
            LEFT JOIN song ON single.song_id = song.id
            LEFT JOIN user ON song.author_id = user.user_id;
        """);
    }

    // REPL
    public static class QueryRepl {
        private final QueryRunner runner;

        public QueryRepl(@NotNull Connection connection) {
            runner = new QueryRunner(connection);
        }

        public void update(@NotNull String query, @Nullable Object @NotNull ... params) throws SQLException {
            System.out.println(">>> " + query.trim());
            System.out.println(runner.runUpdate(query, params));
            System.out.println();
        }

        public void query(@NotNull String query, @Nullable Object @NotNull ... params) throws SQLException {
            System.out.println(">>> " + query.trim());
            try (PreparedStatement statement = runner.prepareQuery(query, params);
                 ResultSet result = statement.executeQuery()) {
                System.out.println(DebugSql.toDebugString(result));
            }
            System.out.println();
        }
    }

    private static Connection connect(SqlSettings settings) throws SQLException {
        return DriverManager.getConnection(settings.url());
    }
}
