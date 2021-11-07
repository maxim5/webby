package io.webby.examples.model;

import io.webby.testing.BaseModelKeyTableTest;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;

import static io.webby.testing.TestingUtil.array;

public class PrimitiveModelTableTest extends BaseModelKeyTableTest<Integer, PrimitiveModel, PrimitiveModelTable> {
    @Override
    protected void setUp(@NotNull Connection connection) throws Exception {
        connection.createStatement().executeUpdate("""
            CREATE TABLE primitive_model (
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
        keys = array(1, 2);
        table = new PrimitiveModelTable(connection);
    }

    @Override
    protected @NotNull PrimitiveModel createEntity(@NotNull Integer key, int version) {
        return new PrimitiveModel(key, version, 1, (byte) 2, (short) 3, 'M', 3.14f, 2.7, version == 0);
    }
}
