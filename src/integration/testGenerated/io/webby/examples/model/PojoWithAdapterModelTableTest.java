package io.webby.examples.model;

import io.webby.orm.api.Connector;
import io.webby.testing.SqliteTableTest;
import io.webby.testing.TableIntTest;
import org.jetbrains.annotations.NotNull;

public class PojoWithAdapterModelTableTest
        extends SqliteTableTest<Integer, PojoWithAdapterModel, PojoWithAdapterModelTable>
        implements TableIntTest<PojoWithAdapterModel, PojoWithAdapterModelTable> {
    @Override
    protected void setUp(@NotNull Connector connector) throws Exception {
        connector.runner().runMultiUpdate("""
            CREATE TABLE pojo_with_adapter_model (
                id INTEGER PRIMARY KEY,
                pojo_id INTEGER,
                pojo_buf TEXT
            )
        """);
        table = new PojoWithAdapterModelTable(connector);
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
