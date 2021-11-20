package io.webby.examples.model;

import io.webby.testing.SqliteTableTest;
import io.webby.testing.TableLongTest;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;

public class NestedModelTableTest
        extends SqliteTableTest<Long, NestedModel, NestedModelTable>
        implements TableLongTest<NestedModel, NestedModelTable> {
    @Override
    protected void setUp(@NotNull Connection connection) throws Exception {
        connection.createStatement().executeUpdate("""
            CREATE TABLE nested_model (
                id INTEGER PRIMARY KEY,
                simple_id INTEGER,
                simple_a INTEGER,
                simple_b TEXT,
                level1_id INTEGER,
                level1_simple_id INTEGER,
                level1_simple_a INTEGER,
                level1_simple_b TEXT
            )
        """);
        table = new NestedModelTable(connection);
    }

    @Override
    public @NotNull NestedModel createEntity(@NotNull Long key, int version) {
        return new NestedModel(key,
                               new NestedModel.Simple(version, 777L, "foo"),
                               new NestedModel.Level1(version, new NestedModel.Simple(version, 0L, "bar")));
    }

    @Override
    public @NotNull NestedModel copyEntityWithId(@NotNull NestedModel entity, long autoId) {
        return new NestedModel(autoId, entity.simple(), entity.level1());
    }
}
