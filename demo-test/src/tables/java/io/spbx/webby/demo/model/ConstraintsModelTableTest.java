package io.spbx.webby.demo.model;

import io.spbx.orm.api.Connector;
import io.spbx.orm.api.query.CreateTableQuery;
import io.spbx.webby.testing.SqlDbTableTest;
import io.spbx.webby.testing.TableIntTest;
import org.jetbrains.annotations.NotNull;

public class ConstraintsModelTableTest
        extends SqlDbTableTest<ConstraintsModel, ConstraintsModelTable>
        implements TableIntTest<ConstraintsModel, ConstraintsModelTable> {
    @Override
    protected void setUp(@NotNull Connector connector) throws Exception {
        table = new ConstraintsModelTable(connector);
        table.admin().createTable(CreateTableQuery.of(table).ifNotExists());
    }

    @Override
    public @NotNull ConstraintsModel createEntity(@NotNull Integer key, int version) {
        int unique = 11 * key + 7 * version;
        return new ConstraintsModel(key, unique, new ConstraintsModel.Range(unique, unique), String.valueOf(version), "");
    }

    @Override
    public @NotNull ConstraintsModel copyEntityWithId(@NotNull ConstraintsModel entity, int autoId) {
        return new ConstraintsModel(autoId, entity.fprint(), entity.range(), entity.displayName(), entity.s());
    }
}
