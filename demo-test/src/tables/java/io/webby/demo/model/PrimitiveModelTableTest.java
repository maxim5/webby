package io.webby.demo.model;

import io.webby.orm.api.Connector;
import io.webby.orm.codegen.SqlSchemaMaker;
import io.webby.testing.TableIntTest;
import io.webby.testing.SqlDbTableTest;
import org.jetbrains.annotations.NotNull;

public class PrimitiveModelTableTest
        extends SqlDbTableTest<Integer, PrimitiveModel, PrimitiveModelTable>
        implements TableIntTest<PrimitiveModel, PrimitiveModelTable> {
    @Override
    protected void setUp(@NotNull Connector connector) throws Exception {
        table = new PrimitiveModelTable(connector);
        connector().runner().runUpdate(SqlSchemaMaker.makeCreateTableQuery(table));
    }

    @Override
    public @NotNull PrimitiveModel createEntity(@NotNull Integer key, int version) {
        return new PrimitiveModel(key, version, 1, (byte) 2, (short) 3, 'M', 3.14f, 2.7, version == 0);
    }

    @Override
    public @NotNull PrimitiveModel copyEntityWithId(@NotNull PrimitiveModel model, int autoId) {
        return new PrimitiveModel(autoId, model.i(), model.l(), model.b(), model.s(), model.ch(), model.f(), model.d(), model.bool());
    }
}
