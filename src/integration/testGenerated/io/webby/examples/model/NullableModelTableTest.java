package io.webby.examples.model;

import io.webby.testing.BaseModelKeyTableTest;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;

import static io.webby.testing.TestingUtil.array;

public class NullableModelTableTest extends BaseModelKeyTableTest<String, NullableModel, NullableModelTable> {
    @Override
    protected void setUp(@NotNull Connection connection) throws Exception {
        connection.createStatement().executeUpdate("""
            CREATE TABLE nullable_model (
                id TEXT PRIMARY KEY,
                str TEXT,
                timestamp INTEGER,
                nested_id INTEGER,
                nested_s TEXT
            )
        """);
        keys = array("foo", "");
        table = new NullableModelTable(connection);
    }

    @Override
    protected @NotNull NullableModel createEntity(@NotNull String key, int version) {
        // TODO: nullable nested
        return new NullableModel(key, null, null, new NullableModel.Nested(version, null));
    }
}
