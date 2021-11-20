package io.webby.examples.model;

import io.webby.testing.PrimaryKeyTableTest;
import io.webby.testing.SqliteTableTest;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;

import static io.webby.testing.TestingUtil.array;

public class WrappersModelTableTest
        extends SqliteTableTest<Integer, WrappersModel, WrappersModelTable>
        implements PrimaryKeyTableTest<Integer, WrappersModel, WrappersModelTable> {
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
        table = new WrappersModelTable(connection);
    }

    @Override
    public @NotNull Integer[] keys() {
        return array(1, 2);
    }

    @Override
    public @NotNull WrappersModel createEntity(@NotNull Integer key, int version) {
        return new WrappersModel(key, version, 1L, (byte) 2, (short) 3, 'M', 3.14f, 2.7, version == 0);
    }
}
