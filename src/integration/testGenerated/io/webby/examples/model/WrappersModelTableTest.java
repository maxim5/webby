package io.webby.examples.model;

import io.webby.testing.BaseModelKeyTableTest;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;

public class WrappersModelTableTest extends BaseModelKeyTableTest<Integer, WrappersModel, WrappersModelTable> {
    @Override
    protected void setUp(@NotNull Connection connection) throws Exception {
        connection.createStatement().executeUpdate("""
        CREATE TABLE wrappers_model (
            id INTEGER PRIMARY KEY,
            i INTEGER,
            l INTEGER,
            b INTEGER,
            s INTEGER,
            ch TEXT,
            f REAL,
            d REAL,
            bool INTEGER
        )
        """);
        key1 = 1;
        key2 = 2;
        table = new WrappersModelTable(connection);
    }

    @Override
    protected @NotNull WrappersModel createEntity(@NotNull Integer key, int version) {
        return new WrappersModel(key, version, 1L, (byte) 2, (short) 3, 'M', 3.14f, 2.7, version == 0);
    }
}
