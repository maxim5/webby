package io.webby.examples.model;

import io.webby.examples.model.DeepNestedModel.A;
import io.webby.examples.model.DeepNestedModel.B;
import io.webby.examples.model.DeepNestedModel.C;
import io.webby.examples.model.DeepNestedModel.D;
import io.webby.testing.PrimaryKeyTableTest;
import io.webby.testing.SqliteTableTest;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;

import static io.webby.testing.TestingUtil.array;

public class DeepNestedModelTableTest
        extends SqliteTableTest<Integer, DeepNestedModel, DeepNestedModelTable>
        implements PrimaryKeyTableTest<Integer, DeepNestedModel, DeepNestedModelTable> {
    @Override
    protected void setUp(@NotNull Connection connection) throws Exception {
        connection.createStatement().executeUpdate("""
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
        table = new DeepNestedModelTable(connection);
    }

    @Override
    public @NotNull Integer[] keys() {
        return array(1, 2);
    }

    @Override
    public @NotNull DeepNestedModel createEntity(@NotNull Integer key, int version) {
        return new DeepNestedModel(
            key,
            new D(1, new C(2, new A(3), new B(4, new A(5)), new A(6)), new B(6, new A(7))),
            new A(key)
        );
    }
}
