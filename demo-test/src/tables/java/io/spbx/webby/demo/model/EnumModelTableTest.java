package io.spbx.webby.demo.model;

import io.spbx.orm.api.Connector;
import io.spbx.orm.api.query.CreateTableQuery;
import io.spbx.orm.api.query.Shortcuts;
import io.spbx.orm.api.query.Variable;
import io.spbx.webby.testing.PrimaryKeyTableTest;
import io.spbx.webby.testing.SqlDbTableTest;
import org.jetbrains.annotations.NotNull;

import static io.spbx.util.testing.TestingBasics.array;

public class EnumModelTableTest
        extends SqlDbTableTest<EnumModel, EnumModelTable>
        implements PrimaryKeyTableTest<EnumModel.Foo, EnumModel, EnumModelTable> {
    @Override
    protected void setUp(@NotNull Connector connector) throws Exception {
        table = new EnumModelTable(connector);
        table.admin().createTable(CreateTableQuery.of(table).ifNotExists());
    }

    @Override
    public @NotNull EnumModel.Foo[] keys() {
        return array(EnumModel.Foo.FIRST, EnumModel.Foo.SECOND);
    }

    @Override
    public @NotNull EnumModel createEntity(@NotNull EnumModel.Foo key, int version) {
        return new EnumModel(key, EnumModel.Foo.FIRST, new EnumModel.Nested(EnumModel.Foo.SECOND, String.valueOf(version)));
    }

    @Override
    public @NotNull Variable keyToVar(@NotNull EnumModel.Foo key) {
        return Shortcuts.var(key.ordinal());
    }
}
