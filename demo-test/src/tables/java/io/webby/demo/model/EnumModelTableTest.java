package io.webby.demo.model;

import io.webby.orm.api.Connector;
import io.webby.orm.codegen.SqlSchemaMaker;
import io.webby.testing.PrimaryKeyTableTest;
import io.webby.testing.SqlDbTableTest;
import org.jetbrains.annotations.NotNull;

import static io.webby.testing.TestingUtil.array;

public class EnumModelTableTest
        extends SqlDbTableTest<EnumModel, EnumModelTable>
        implements PrimaryKeyTableTest<EnumModel.Foo, EnumModel, EnumModelTable> {
    @Override
    protected void setUp(@NotNull Connector connector) throws Exception {
        table = new EnumModelTable(connector);
        connector().runner().runUpdate(SqlSchemaMaker.makeCreateTableQuery(table));
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
