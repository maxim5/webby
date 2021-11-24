package io.webby.examples.model;

import io.webby.orm.api.Connector;
import io.webby.testing.SqliteTableTest;
import io.webby.testing.TableIntTest;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicInteger;

public class AtomicModelTableTest
        extends SqliteTableTest<Integer, AtomicModel, AtomicModelTable>
        implements TableIntTest<AtomicModel, AtomicModelTable> {
    @Override
    protected void setUp(@NotNull Connector connector) throws Exception {
        connector.runner().runMultiUpdate("""
            CREATE TABLE atomic_model (
                id INTEGER PRIMARY KEY,
                i INTEGER
            )
        """);
        table = new AtomicModelTable(connector);
    }

    @Override
    public @NotNull AtomicModel createEntity(@NotNull Integer key, int version) {
        return new AtomicModel(key, new AtomicInteger(version));
    }

    @Override
    public @NotNull AtomicModel copyEntityWithId(@NotNull AtomicModel entity, int autoId) {
        return new AtomicModel(autoId, entity.i());
    }
}
