package io.spbx.webby.demo.model;

import io.spbx.orm.api.Connector;
import io.spbx.orm.api.query.CreateTableQuery;
import io.spbx.webby.testing.BaseTableTest;
import io.spbx.webby.testing.SqlDbTableTest;
import org.jetbrains.annotations.NotNull;

public class UserRateModelTableTest
        extends SqlDbTableTest<UserRateModel, UserRateModelTable>
        implements BaseTableTest<UserRateModel, UserRateModelTable> {
    @Override
    protected void setUp(@NotNull Connector connector) throws Exception {
        table = new UserRateModelTable(connector);
        table.admin().createTable(CreateTableQuery.of(table).ifNotExists());
    }
}
