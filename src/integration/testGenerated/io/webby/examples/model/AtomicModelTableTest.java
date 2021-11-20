package io.webby.examples.model;

import io.webby.testing.PrimaryKeyTableTest;
import io.webby.testing.SqliteTableTest;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.util.concurrent.atomic.AtomicInteger;

import static io.webby.testing.TestingUtil.array;

public class AtomicModelTableTest
        extends SqliteTableTest<Integer, AtomicModel, AtomicModelTable>
        implements PrimaryKeyTableTest<Integer, AtomicModel, AtomicModelTable> {
    @Override
    protected void setUp(@NotNull Connection connection) throws Exception {
        connection.createStatement().executeUpdate("""
            CREATE TABLE atomic_model (
                id TEXT PRIMARY KEY,
                i INTEGER
            )
        """);
        table = new AtomicModelTable(connection);
    }

    @Override
    public @NotNull Integer[] keys() {
        return array(1, 2);
    }

    @Override
    public @NotNull AtomicModel createEntity(@NotNull Integer key, int version) {
        return new AtomicModel(key, new AtomicInteger(version));
    }
}
