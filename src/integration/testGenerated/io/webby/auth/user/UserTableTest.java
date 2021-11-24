package io.webby.auth.user;

import io.webby.orm.api.Connector;
import io.webby.testing.SqliteTableTest;
import io.webby.testing.TableLongTest;
import org.jetbrains.annotations.NotNull;

public class UserTableTest
        extends SqliteTableTest<Long, DefaultUser, UserTable>
        implements TableLongTest<DefaultUser, UserTable> {
    @Override
    protected void setUp(@NotNull Connector connector) throws Exception {
        connector.runner().runMultiUpdate("""
            CREATE TABLE user (
                user_id INTEGER PRIMARY KEY,
                access_level INTEGER
            )
        """);
        table = new UserTable(connector);
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
