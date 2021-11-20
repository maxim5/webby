package io.webby.auth.user;

import io.webby.testing.PrimaryKeyTableTest;
import io.webby.testing.SqliteTableTest;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;

import static io.webby.testing.TestingUtil.array;

public class UserTableTest
        extends SqliteTableTest<Long, DefaultUser, UserTable>
        implements PrimaryKeyTableTest<Long, DefaultUser, UserTable> {
    @Override
    protected void setUp(@NotNull Connection connection) throws Exception {
        connection.createStatement().executeUpdate("""
            CREATE TABLE user (
                user_id INTEGER PRIMARY KEY,
                access_level INTEGER
            )
        """);
        table = new UserTable(connection);
    }

    @Override
    public @NotNull Long[] keys() {
        return array(1L, 2L);
    }

    @Override
    public @NotNull DefaultUser createEntity(@NotNull Long key, int version) {
        return new DefaultUser(key, new UserAccess(version + 1));
    }
}
