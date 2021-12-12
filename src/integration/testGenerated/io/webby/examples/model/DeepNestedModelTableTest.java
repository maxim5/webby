package io.webby.examples.model;

import io.webby.examples.model.DeepNestedModel.A;
import io.webby.examples.model.DeepNestedModel.B;
import io.webby.examples.model.DeepNestedModel.C;
import io.webby.examples.model.DeepNestedModel.D;
import io.webby.orm.api.Connector;
import io.webby.orm.codegen.SqlSchemaMaker;
import io.webby.testing.SqlDbTableTest;
import io.webby.testing.TableIntTest;
import org.jetbrains.annotations.NotNull;

public class DeepNestedModelTableTest
        extends SqlDbTableTest<Integer, DeepNestedModel, DeepNestedModelTable>
        implements TableIntTest<DeepNestedModel, DeepNestedModelTable> {
    @Override
    protected void setUp(@NotNull Connector connector) throws Exception {
        table = new DeepNestedModelTable(connector);
        connector().runner().runMultiUpdate(SqlSchemaMaker.makeCreateTableQuery(table));
    }

    @Override
    public @NotNull DeepNestedModel createEntity(@NotNull Integer key, int version) {
        return new DeepNestedModel(
            key,
            new D(1, new C(2, new A(3), new B(4, new A(5)), new A(6)), new B(6, new A(7))),
            new A(key)
        );
    }

    @Override
    public @NotNull DeepNestedModel copyEntityWithId(@NotNull DeepNestedModel entity, int autoId) {
        return new DeepNestedModel(autoId, entity.d(), entity.a());
    }
}
