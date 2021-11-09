package io.webby.examples.model;

import io.webby.testing.BaseModelKeyTableTest;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;

import static io.webby.testing.TestingUtil.array;

public class InheritedModelTableTest extends BaseModelKeyTableTest<Integer, InheritedModel, InheritedModelTable> {
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
        keys = array(1, 2);
        table = new InheritedModelTable(connection);
    }

    @Override
    protected @NotNull InheritedModel createEntity(@NotNull Integer key, int version) {
        return new InheritedModel(String.valueOf(version), version, key, version == 0);
    }
}
