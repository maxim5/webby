package io.webby.demo.model;

import io.webby.orm.api.Connector;
import io.webby.orm.codegen.SqlSchemaMaker;
import io.webby.testing.TableIntTest;
import io.webby.testing.SqlDbTableTest;
import org.jetbrains.annotations.NotNull;

public class PojoWithAdapterModelTableTest
        extends SqlDbTableTest<Integer, PojoWithAdapterModel, PojoWithAdapterModelTable>
        implements TableIntTest<PojoWithAdapterModel, PojoWithAdapterModelTable> {
    @Override
    protected void setUp(@NotNull Connector connector) throws Exception {
        table = new PojoWithAdapterModelTable(connector);
        connector().runner().runUpdate(SqlSchemaMaker.makeCreateTableQuery(table));
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
