package io.webby.examples.model;

import io.webby.examples.model.DeepNestedModel.A;
import io.webby.examples.model.DeepNestedModel.B;
import io.webby.examples.model.DeepNestedModel.C;
import io.webby.examples.model.DeepNestedModel.D;
import io.webby.orm.api.Connector;
import io.webby.testing.SqliteTableTest;
import io.webby.testing.TableIntTest;
import org.jetbrains.annotations.NotNull;

public class DeepNestedModelTableTest
        extends SqliteTableTest<Integer, DeepNestedModel, DeepNestedModelTable>
        implements TableIntTest<DeepNestedModel, DeepNestedModelTable> {
    @Override
    protected void setUp(@NotNull Connector connector) throws Exception {
        connector.runner().runMultiUpdate("""
            CREATE TABLE deep_nested_model (
                id INTEGER PRIMARY KEY,
                d_id INTEGER,
                d_c_id INTEGER,
                d_c_a_id INTEGER,
                d_c_b_id INTEGER,
                d_c_b_a_id INTEGER,
                d_c_aa_id INTEGER,
                d_b_id INTEGER,
                d_b_a_id INTEGER,
                a_id INTEGER
            )
        """);
        table = new DeepNestedModelTable(connector);
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
