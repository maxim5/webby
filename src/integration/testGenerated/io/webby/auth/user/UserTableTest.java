package io.webby.auth.user;

import io.webby.orm.api.Connector;
import io.webby.orm.codegen.SqlSchemaMaker;
import io.webby.testing.SqlDbTableTest;
import io.webby.testing.TableLongTest;
import org.jetbrains.annotations.NotNull;

public class UserTableTest
        extends SqlDbTableTest<Long, DefaultUser, UserTable>
        implements TableLongTest<DefaultUser, UserTable> {
    @Override
    protected void setUp(@NotNull Connector connector) throws Exception {
        table = new UserTable(connector);
        connector().runner().runMultiUpdate(SqlSchemaMaker.makeCreateTableQuery(table));
    }

    @Override
    public @NotNull DefaultUser createEntity(@NotNull Long key, int version) {
        return new DefaultUser(key, new UserAccess(version + 1));
    }

    @Override
    public @NotNull DefaultUser copyEntityWithId(@NotNull DefaultUser user, long autoId) {
        return new DefaultUser(autoId, user.access());
    }
}
