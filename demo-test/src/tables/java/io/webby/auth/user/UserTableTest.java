package io.webby.auth.user;

import io.webby.orm.api.Connector;
import io.webby.testing.SqlDbTableTest;
import io.webby.testing.TableIntTest;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class UserTableTest
        extends SqlDbTableTest<DefaultUser, UserTable>
        implements TableIntTest<DefaultUser, UserTable> {
    @Override
    protected void setUp(@NotNull Connector connector) throws Exception {
        table = new UserTable(connector);
        table.admin().createTableIfNotExists(table.meta());
    }

    @Override
    public @NotNull DefaultUser createEntity(@NotNull Integer key, int version) {
        Instant created = Instant.now().truncatedTo(ChronoUnit.MILLIS);
        return new DefaultUser(key, created, new UserAccess(version + 1));
    }

    @Override
    public @NotNull DefaultUser copyEntityWithId(@NotNull DefaultUser user, int autoId) {
        return new DefaultUser(autoId, user.createdAt(), user.access());
    }
}
