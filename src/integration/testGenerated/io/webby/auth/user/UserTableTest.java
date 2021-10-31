package io.webby.auth.user;

import io.webby.testing.BaseModelKeyTableTest;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;

public class UserTableTest extends BaseModelKeyTableTest<Long, DefaultUser, UserTable> {
    @Override
    protected void setUp(@NotNull Connection connection) throws Exception {
        connection.createStatement().executeUpdate("CREATE TABLE user (user_id INTEGER PRIMARY KEY, access INTEGER)");
        key1 = 1L;
        key2 = 2L;
        table = new UserTable(connection);
    }

    @Override
    protected @NotNull DefaultUser createEntity(@NotNull Long key, int version) {
        return new DefaultUser(key, new UserAccess(version + 1));
    }
}
