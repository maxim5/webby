package io.webby.demo.model;

import io.webby.orm.api.Connector;
import io.webby.testing.SqlDbTableTest;
import io.webby.testing.TableLongTest;
import org.jetbrains.annotations.NotNull;

public class NestedModelTableTest
        extends SqlDbTableTest<NestedModel, NestedModelTable>
        implements TableLongTest<NestedModel, NestedModelTable> {
    @Override
    protected void setUp(@NotNull Connector connector) throws Exception {
        table = new NestedModelTable(connector);
        table.admin().createTable(table.meta()).ifNotExists().run();
    }

    @Override
    public @NotNull NestedModel createEntity(@NotNull Long key, int version) {
        return new NestedModel(key,
                               new NestedModel.Simple(version, 777L, "foo"),
                               new NestedModel.Level1(version, new NestedModel.Simple(version, 0L, "bar")));
    }

    @Override
    public @NotNull NestedModel copyEntityWithId(@NotNull NestedModel entity, long autoId) {
        return new NestedModel(autoId, entity.simple(), entity.level1());
    }
}
