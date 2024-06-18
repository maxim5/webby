package io.spbx.webby.auth.user;

import io.spbx.orm.api.Connector;
import io.spbx.orm.api.query.CreateTableQuery;
import io.webby.testing.SqlDbTableTest;
import io.webby.testing.TableIntTest;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;

public class UserTableTest
        extends SqlDbTableTest<DefaultUser, UserTable>
        implements TableIntTest<DefaultUser, UserTable> {
    @Override
    protected void setUp(@NotNull Connector connector) throws Exception {
        table = new UserTable(connector);
        table.admin().createTable(CreateTableQuery.of(table).ifNotExists());
    }

    @Override
    public @NotNull DefaultUser createEntity(@NotNull Integer key, int version) {
        return DefaultUser.newUser(key, Instant.now(), new UserAccess(version + 1));
    }

    @Override
    public @NotNull DefaultUser copyEntityWithId(@NotNull DefaultUser user, int autoId) {
        return DefaultUser.newUser(autoId, user.createdAt(), user.access());
    }
}
