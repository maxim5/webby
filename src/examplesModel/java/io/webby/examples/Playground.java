package io.webby.examples;

import io.webby.util.sql.api.DebugSql.DebugRunner;

import java.sql.Connection;
import java.sql.DriverManager;

public class Playground {
    private static final String URL = "jdbc:sqlite:%s".formatted(":memory:");

    public static void main(String[] args) throws Exception {
        Connection connection = DriverManager.getConnection(URL);
        DebugRunner main = new DebugRunner(connection);

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

        main.update("CREATE TABLE blob (blob_id BLOB PRIMARY KEY, blob_value BLOB)");
        main.update("INSERT INTO blob(blob_id, blob_value) VALUES(?, ?)", "foo".getBytes(), "bar".getBytes());
        main.query("SELECT * FROM blob");
    }
}
