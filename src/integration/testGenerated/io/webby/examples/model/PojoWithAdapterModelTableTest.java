package io.webby.examples.model;

import io.webby.testing.SqliteTableTest;
import io.webby.testing.TableIntTest;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;

public class PojoWithAdapterModelTableTest
        extends SqliteTableTest<Integer, PojoWithAdapterModel, PojoWithAdapterModelTable>
        implements TableIntTest<PojoWithAdapterModel, PojoWithAdapterModelTable> {
    @Override
    protected void setUp(@NotNull Connection connection) throws Exception {
        connection.createStatement().executeUpdate("""
            CREATE TABLE pojo_with_adapter_model (
                id INTEGER PRIMARY KEY,
                pojo_id INTEGER,
                pojo_buf TEXT
            )
        """);
        table = new PojoWithAdapterModelTable(connection);
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
