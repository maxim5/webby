package io.spbx.webby.demo.model;

import io.spbx.orm.api.Connector;
import io.spbx.orm.api.query.Column;
import io.spbx.orm.api.query.CreateTableQuery;
import io.spbx.orm.api.query.Shortcuts;
import io.spbx.orm.api.query.Variable;
import io.spbx.webby.testing.PrimaryKeyTableTest;
import io.spbx.webby.testing.SqlDbTableTest;
import org.jetbrains.annotations.NotNull;

import static io.spbx.util.testing.TestingBasics.array;

public class ComplexIdModelTableTest
        extends SqlDbTableTest<ComplexIdModel, ComplexIdModelTable>
        implements PrimaryKeyTableTest<ComplexIdModel.Key, ComplexIdModel, ComplexIdModelTable> {
    @Override
    protected void setUp(@NotNull Connector connector) throws Exception {
        table = new ComplexIdModelTable(connector);
        table.admin().createTable(CreateTableQuery.of(table).ifNotExists());
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
