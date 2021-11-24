package io.webby.examples.model;

import io.webby.orm.api.Connector;
import io.webby.testing.PrimaryKeyTableTest;
import io.webby.testing.SqliteTableTest;
import org.jetbrains.annotations.NotNull;

import static io.webby.testing.TestingUtil.array;

public class EnumModelTableTest
        extends SqliteTableTest<EnumModel.Foo, EnumModel, EnumModelTable>
        implements PrimaryKeyTableTest<EnumModel.Foo, EnumModel, EnumModelTable> {
    @Override
    protected void setUp(@NotNull Connector connector) throws Exception {
        connector.runner().runMultiUpdate("""
            CREATE TABLE enum_model (
                id_ord INTEGER PRIMARY KEY,
                foo_ord INTEGER,
                nested_foo_ord INTEGER,
                nested_s TEXT
            )
        """);
        table = new EnumModelTable(connector);
    }

    @Override
    public @NotNull EnumModel.Foo[] keys() {
        return array(EnumModel.Foo.FIRST, EnumModel.Foo.SECOND);
    }

    @Override
    public @NotNull EnumModel createEntity(EnumModel.@NotNull Foo key, int version) {
        return new EnumModel(key, EnumModel.Foo.FIRST, new EnumModel.Nested(EnumModel.Foo.SECOND, String.valueOf(version)));
    }
}
