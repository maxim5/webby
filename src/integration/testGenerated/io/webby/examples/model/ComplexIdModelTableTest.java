package io.webby.examples.model;

import io.webby.orm.api.Connector;
import io.webby.orm.codegen.SqlSchemaMaker;
import io.webby.testing.PrimaryKeyTableTest;
import io.webby.testing.SqlDbTableTest;
import org.jetbrains.annotations.NotNull;

import static io.webby.testing.TestingUtil.array;

public class ComplexIdModelTableTest
        extends SqlDbTableTest<ComplexIdModel.Key, ComplexIdModel, ComplexIdModelTable>
        implements PrimaryKeyTableTest<ComplexIdModel.Key, ComplexIdModel, ComplexIdModelTable> {
    @Override
    protected void setUp(@NotNull Connector connector) throws Exception {
        table = new ComplexIdModelTable(connector);
        connector().runner().runMultiUpdate(SqlSchemaMaker.makeCreateTableQuery(table));
    }

    @Override
    public @NotNull ComplexIdModel.Key[] keys() {
        return array(new ComplexIdModel.Key(1, 1, "1"), new ComplexIdModel.Key(2, 2, "2"), new ComplexIdModel.Key(3, 3, "3"));
    }

    @Override
    public @NotNull ComplexIdModel createEntity(ComplexIdModel.@NotNull Key key, int version) {
        return new ComplexIdModel(key, version);
    }
}
