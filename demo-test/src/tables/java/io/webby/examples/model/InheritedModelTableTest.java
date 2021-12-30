package io.webby.examples.model;

import io.webby.orm.api.Connector;
import io.webby.orm.codegen.SqlSchemaMaker;
import io.webby.testing.SqlDbTableTest;
import io.webby.testing.TableIntTest;
import org.jetbrains.annotations.NotNull;

public class InheritedModelTableTest
        extends SqlDbTableTest<Integer, InheritedModel, InheritedModelTable>
        implements TableIntTest<InheritedModel, InheritedModelTable> {
    @Override
    protected void setUp(@NotNull Connector connector) throws Exception {
        table = new InheritedModelTable(connector);
        connector().runner().runUpdate(SqlSchemaMaker.makeCreateTableQuery(table));
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
