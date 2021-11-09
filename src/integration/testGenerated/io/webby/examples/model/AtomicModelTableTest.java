package io.webby.examples.model;

import com.google.common.primitives.Ints;
import io.webby.testing.BaseModelKeyTableTest;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.util.concurrent.atomic.AtomicInteger;

import static io.webby.testing.TestingUtil.array;

public class AtomicModelTableTest extends BaseModelKeyTableTest<Integer, AtomicModel, AtomicModelTable> {
    @Override
    protected void setUp(@NotNull Connection connection) throws Exception {
        connection.createStatement().executeUpdate("""
            CREATE TABLE atomic_model (
                id TEXT PRIMARY KEY,
                i INTEGER
            )
        """);
        keys = array(1, 2);
        table = new AtomicModelTable(connection);
    }

    @Override
    protected @NotNull AtomicModel createEntity(@NotNull Integer key, int version) {
        return new AtomicModel(key, new AtomicInteger(version));
    }
}
