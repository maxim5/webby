package io.webby.demo.model;

import io.webby.orm.api.Connector;
import io.webby.orm.api.query.CreateTableQuery;
import io.webby.testing.PrimaryKeyTableTest;
import io.webby.testing.SqlDbTableTest;
import org.jetbrains.annotations.NotNull;

import static io.webby.testing.TestingBasics.array;

public class WrappersModelTableTest
        extends SqlDbTableTest<WrappersModel, WrappersModelTable>
        implements PrimaryKeyTableTest<Integer, WrappersModel, WrappersModelTable> {
    @Override
    protected void setUp(@NotNull Connector connector) throws Exception {
        table = new WrappersModelTable(connector);
        table.admin().createTable(CreateTableQuery.of(table).ifNotExists());
    }

    @Override
    public @NotNull Integer[] keys() {
        return array(1, 2, 3, 4, 5);
    }

    @Override
    public @NotNull WrappersModel createEntity(@NotNull Integer key, int version) {
        return new WrappersModel(key, version, 1L, (byte) 2, (short) 3, 'M', 3.14f, 2.7, version == 0);
    }
}
