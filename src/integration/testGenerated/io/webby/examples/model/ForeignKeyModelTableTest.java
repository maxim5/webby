package io.webby.examples.model;

import io.webby.testing.BaseModelKeyTableTest;
import io.webby.util.sql.api.ForeignInt;
import io.webby.util.sql.api.ForeignLong;
import io.webby.util.sql.api.ForeignObj;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;

import static io.webby.testing.TestingUtil.array;

public class ForeignKeyModelTableTest extends BaseModelKeyTableTest<Long, ForeignKeyModel, ForeignKeyModelTable> {
    @Override
    protected void setUp(@NotNull Connection connection) throws Exception {
        connection.createStatement().executeUpdate("""
            CREATE TABLE f_k_int (
                id INTEGER PRIMARY KEY,
                value INTEGER
            )
        """);
        connection.createStatement().executeUpdate("""
            CREATE TABLE f_k_long (
                id INTEGER PRIMARY KEY,
                value INTEGER
            )
        """);
        connection.createStatement().executeUpdate("""
            CREATE TABLE f_k_string (
                id TEXT PRIMARY KEY,
                value TEXT
            )
        """);
        connection.createStatement().executeUpdate("""
            CREATE TABLE foreign_key_model (
                id INTEGER PRIMARY KEY,
                inner_int_id INTEGER,
                inner_long_id INTEGER,
                inner_string_id TEXT
            )
        """);
        keys = array(1L, 2L);
        table = new ForeignKeyModelTable(connection);
    }

    @Override
    protected @NotNull ForeignKeyModel createEntity(@NotNull Long key, int version) {
        return new ForeignKeyModel(key, ForeignInt.ofId(version), ForeignLong.ofId(777), ForeignObj.ofId("foo"));
    }
}
