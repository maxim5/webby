package io.webby.examples.model;

import io.webby.testing.BaseModelKeyTableTest;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;

import static io.webby.testing.TestingUtil.array;

public class EnumModelTableTest extends BaseModelKeyTableTest<EnumModel.Foo, EnumModel, EnumModelTable> {
    @Override
    protected void setUp(@NotNull Connection connection) throws Exception {
        connection.createStatement().executeUpdate("""
            CREATE TABLE enum_model (
                id_ord INTEGER PRIMARY KEY,
                foo_ord INTEGER,
                nested_foo_ord INTEGER,
                nested_s TEXT
            )
        """);
        keys = array(EnumModel.Foo.FIRST, EnumModel.Foo.SECOND);
        table = new EnumModelTable(connection);
    }

    @Override
    protected @NotNull EnumModel createEntity(EnumModel.@NotNull Foo key, int version) {
        return new EnumModel(key, EnumModel.Foo.FIRST, new EnumModel.Nested(EnumModel.Foo.SECOND, String.valueOf(version)));
    }
}
