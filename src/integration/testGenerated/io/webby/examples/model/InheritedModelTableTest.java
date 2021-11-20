package io.webby.examples.model;

import io.webby.testing.PrimaryKeyTableTest;
import io.webby.testing.SqliteTableTest;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;

import static io.webby.testing.TestingUtil.array;

public class InheritedModelTableTest
        extends SqliteTableTest<Integer, InheritedModel, InheritedModelTable>
        implements PrimaryKeyTableTest<Integer, InheritedModel, InheritedModelTable> {
    @Override
    protected void setUp(@NotNull Connection connection) throws Exception {
        connection.createStatement().executeUpdate("""
            CREATE TABLE inherited_model (
                str TEXT,
                int_value INTEGER,
                inherited_model_id INTEGER PRIMARY KEY,
                bool_value INTEGER
            )
        """);
        table = new InheritedModelTable(connection);
    }

    @Override
    public @NotNull Integer[] keys() {
        return array(1, 2);
    }

    @Override
    public @NotNull InheritedModel createEntity(@NotNull Integer key, int version) {
        return new InheritedModel(String.valueOf(version), version, key, version == 0);
    }
}
