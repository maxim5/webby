package io.webby.examples.model;

import io.webby.testing.BaseModelKeyTableTest;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;

import static io.webby.testing.TestingUtil.array;

public class PojoWithAdapterModelTableTest extends BaseModelKeyTableTest<Integer, PojoWithAdapterModel, PojoWithAdapterModelTable> {
    @Override
    protected void setUp(@NotNull Connection connection) throws Exception {
        connection.createStatement().executeUpdate("""
            CREATE TABLE pojo_with_adapter_model (
                id INTEGER PRIMARY KEY,
                pojo_id INTEGER,
                pojo_buf TEXT
            )
        """);
        keys = array(1, 2);
        table = new PojoWithAdapterModelTable(connection);
    }

    @Override
    protected @NotNull PojoWithAdapterModel createEntity(@NotNull Integer key, int version) {
        return new PojoWithAdapterModel(key, new PojoWithAdapterModel.Pojo(version, String.valueOf(version).toCharArray()));
    }
}
