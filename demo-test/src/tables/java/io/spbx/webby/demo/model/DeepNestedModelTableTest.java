package io.spbx.webby.demo.model;

import io.spbx.orm.api.Connector;
import io.spbx.orm.api.query.CreateTableQuery;
import io.spbx.webby.demo.model.DeepNestedModel.A;
import io.spbx.webby.demo.model.DeepNestedModel.B;
import io.spbx.webby.demo.model.DeepNestedModel.C;
import io.spbx.webby.demo.model.DeepNestedModel.D;
import io.spbx.webby.testing.SqlDbTableTest;
import io.spbx.webby.testing.TableIntTest;
import org.jetbrains.annotations.NotNull;

public class DeepNestedModelTableTest
        extends SqlDbTableTest<DeepNestedModel, DeepNestedModelTable>
        implements TableIntTest<DeepNestedModel, DeepNestedModelTable> {
    @Override
    protected void setUp(@NotNull Connector connector) throws Exception {
        table = new DeepNestedModelTable(connector);
        table.admin().createTable(CreateTableQuery.of(table).ifNotExists());
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
