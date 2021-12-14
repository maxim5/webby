package io.webby.examples.model;

import io.webby.orm.api.Connector;
import io.webby.orm.codegen.SqlSchemaMaker;
import io.webby.testing.SqlDbTableTest;
import io.webby.testing.TableIntTest;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class AtomicModelTableTest
        extends SqlDbTableTest<Integer, AtomicModel, AtomicModelTable>
        implements TableIntTest<AtomicModel, AtomicModelTable> {
    @Override
    protected void setUp(@NotNull Connector connector) throws Exception {
        table = new AtomicModelTable(connector);
        connector().runner().runUpdate(SqlSchemaMaker.makeCreateTableQuery(table));
    }

    @Override
    public @NotNull AtomicModel createEntity(@NotNull Integer key, int version) {
        return new AtomicModel(key, new AtomicInteger(version), new AtomicLong(version), new AtomicBoolean(version > 0));
    }

    @Override
    public @NotNull AtomicModel copyEntityWithId(@NotNull AtomicModel entity, int autoId) {
        return new AtomicModel(autoId, entity.i(), entity.l(), entity.b());
    }
}
