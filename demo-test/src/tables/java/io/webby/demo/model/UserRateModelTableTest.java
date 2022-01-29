package io.webby.demo.model;

import io.webby.orm.api.Connector;
import io.webby.orm.codegen.SqlSchemaMaker;
import io.webby.testing.BaseTableTest;
import io.webby.testing.SqlDbTableTest;
import org.jetbrains.annotations.NotNull;

public class UserRateModelTableTest
        extends SqlDbTableTest<UserRateModel, UserRateModelTable>
        implements BaseTableTest<UserRateModel, UserRateModelTable> {
    @Override
    protected void setUp(@NotNull Connector connector) throws Exception {
        table = new UserRateModelTable(connector);
        connector().runner().runUpdate(SqlSchemaMaker.makeCreateTableQuery(table));
    }
}
