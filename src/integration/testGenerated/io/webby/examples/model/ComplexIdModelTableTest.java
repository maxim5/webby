package io.webby.examples.model;

import io.webby.testing.BaseModelKeyTableTest;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;

import static io.webby.testing.TestingUtil.array;

public class ComplexIdModelTableTest extends BaseModelKeyTableTest<ComplexIdModel.Key, ComplexIdModel, ComplexIdModelTable> {
    @Override
    protected void setUp(@NotNull Connection connection) throws Exception {
        connection.createStatement().executeUpdate("""
            CREATE TABLE complex_id_model (
                id_x INTEGER,
                id_y INTEGER,
                id_z TEXT,
                a INTEGER
            )
        """);
        keys = array(new ComplexIdModel.Key(1, 1, "1"), new ComplexIdModel.Key(2, 2, "2"));
        table = new ComplexIdModelTable(connection);
    }

    @Override
    protected @NotNull ComplexIdModel createEntity(ComplexIdModel.@NotNull Key key, int version) {
        return new ComplexIdModel(key, version);
    }
}
