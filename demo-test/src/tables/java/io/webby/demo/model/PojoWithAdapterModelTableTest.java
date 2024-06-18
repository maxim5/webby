package io.webby.demo.model;

import io.spbx.orm.api.Connector;
import io.spbx.orm.api.query.CreateTableQuery;
import io.webby.testing.SqlDbTableTest;
import io.webby.testing.TableIntTest;
import org.jetbrains.annotations.NotNull;

public class PojoWithAdapterModelTableTest
        extends SqlDbTableTest<PojoWithAdapterModel, PojoWithAdapterModelTable>
        implements TableIntTest<PojoWithAdapterModel, PojoWithAdapterModelTable> {
    @Override
    protected void setUp(@NotNull Connector connector) throws Exception {
        table = new PojoWithAdapterModelTable(connector);
        table.admin().createTable(CreateTableQuery.of(table).ifNotExists());
    }

    @Override
    public @NotNull PojoWithAdapterModel createEntity(@NotNull Integer key, int version) {
        return new PojoWithAdapterModel(key, new PojoWithAdapterModel.Pojo(version, String.valueOf(version).toCharArray()));
    }

    @Override
    public @NotNull PojoWithAdapterModel copyEntityWithId(@NotNull PojoWithAdapterModel entity, int autoId) {
        return new PojoWithAdapterModel(autoId, entity.pojo());
    }
}
