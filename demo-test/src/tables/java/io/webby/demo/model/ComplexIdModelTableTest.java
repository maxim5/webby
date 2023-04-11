package io.webby.demo.model;

import io.webby.orm.api.Connector;
import io.webby.orm.api.query.Column;
import io.webby.orm.api.query.Shortcuts;
import io.webby.orm.api.query.Variable;
import io.webby.orm.codegen.SqlSchemaMaker;
import io.webby.testing.PrimaryKeyTableTest;
import io.webby.testing.SqlDbTableTest;
import org.jetbrains.annotations.NotNull;

import static io.webby.testing.TestingUtil.array;

public class ComplexIdModelTableTest
        extends SqlDbTableTest<ComplexIdModel, ComplexIdModelTable>
        implements PrimaryKeyTableTest<ComplexIdModel.Key, ComplexIdModel, ComplexIdModelTable> {
    @Override
    protected void setUp(@NotNull Connector connector) throws Exception {
        table = new ComplexIdModelTable(connector);
        connector().runner().runUpdate(SqlSchemaMaker.makeCreateTableQuery(table));
    }

    @Override
    public @NotNull ComplexIdModel.Key[] keys() {
        return array(new ComplexIdModel.Key(1, 1, "1"), new ComplexIdModel.Key(2, 2, "2"), new ComplexIdModel.Key(3, 3, "3"));
    }

    @Override
    public @NotNull ComplexIdModel createEntity(@NotNull ComplexIdModel.Key key, int version) {
        return new ComplexIdModel(key, version);
    }

    @Override
    public @NotNull Column findPkColumnOrDie() {
        return ComplexIdModelTable.OwnColumn.id_x;  // use only id.x
    }

    @Override
    public @NotNull Variable keyToVar(@NotNull ComplexIdModel.Key key) {
        return Shortcuts.var(key.x());  // use only id.x
    }
}
