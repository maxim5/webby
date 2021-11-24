package io.webby.examples.model;

import io.webby.orm.api.Connector;
import io.webby.testing.PrimaryKeyTableTest;
import io.webby.testing.SqliteTableTest;
import org.jetbrains.annotations.NotNull;

import static io.webby.testing.TestingUtil.array;

public class ComplexIdModelTableTest
        extends SqliteTableTest<ComplexIdModel.Key, ComplexIdModel, ComplexIdModelTable>
        implements PrimaryKeyTableTest<ComplexIdModel.Key, ComplexIdModel, ComplexIdModelTable> {
    @Override
    protected void setUp(@NotNull Connector connector) throws Exception {
        connector.runner().runMultiUpdate("""
            CREATE TABLE complex_id_model (
                id_x INTEGER,
                id_y INTEGER,
                id_z TEXT,
                a INTEGER
            )
        """);
        table = new ComplexIdModelTable(connector);
    }

    @Override
    public @NotNull ComplexIdModel.Key[] keys() {
        return array(new ComplexIdModel.Key(1, 1, "1"), new ComplexIdModel.Key(2, 2, "2"));
    }

    @Override
    public @NotNull ComplexIdModel createEntity(ComplexIdModel.@NotNull Key key, int version) {
        return new ComplexIdModel(key, version);
    }
}
