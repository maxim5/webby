package io.webby.demo.model;

import io.webby.orm.api.Connector;
import io.webby.orm.api.query.CreateTableQuery;
import io.webby.testing.BaseTableTest;
import io.webby.testing.SqlDbTableTest;
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
