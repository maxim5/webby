package io.webby.examples.model;

import io.webby.testing.SqliteTableTest;
import io.webby.testing.TableIntTest;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;

public class InheritedModelTableTest
        extends SqliteTableTest<Integer, InheritedModel, InheritedModelTable>
        implements TableIntTest<InheritedModel, InheritedModelTable> {
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
    public @NotNull InheritedModel createEntity(@NotNull Integer key, int version) {
        return new InheritedModel(String.valueOf(version), version, key, version == 0);
    }

    @Override
    public @NotNull InheritedModel copyEntityWithId(@NotNull InheritedModel entity, int autoId) {
        return new InheritedModel(entity.getStr(), entity.getIntValue(), autoId, entity.isBoolValue());
    }
}
