package io.webby.db.sql;

import io.webby.orm.api.Connector;
import io.webby.orm.api.Engine;
import io.webby.orm.api.QueryRunner;
import io.webby.util.base.Unchecked;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;

public class ThreadLocalConnector implements Connector {
    private static final ThreadLocal<ConnectionData> local = new ThreadLocal<>();

    private final ConnectionPool pool;
    private final long timeoutToExpireMillis;

    public ThreadLocalConnector(@NotNull ConnectionPool pool, long timeoutToExpireMillis) {
        this.pool = pool;
        this.timeoutToExpireMillis = timeoutToExpireMillis;
    }

    @Override
    public @NotNull Connection connection() {
        ConnectionData data = local.get();
        if (data != null && data.isOpen()) {
            return data.connection;
        }
        local.set(connectNow());
        return local.get().connection;
    }

    @Override
    public @NotNull QueryRunner runner() {
        ConnectionData data = local.get();
        if (data != null && data.isOpen()) {
            return data.runner;
        }
        local.set(connectNow());
        return local.get().runner;
    }

    @Override
    public @NotNull Engine engine() {
        return pool.engine();
    }

    public void refreshIfNecessary() {
        ConnectionData data = local.get();
        if (data != null && now() > data.expireEpochMillis) {
            data.close();
            local.set(connectNow());
        }
    }

    public static void cleanupIfNecessary() {
        ConnectionData data = local.get();
        if (data != null && now() > data.expireEpochMillis) {
            data.close();
            local.remove();
        }
    }

    public static void forceCleanUp() {
        ConnectionData data = local.get();
        if (data != null) {
            data.close();
            local.remove();
        }
    }

    private @NotNull ConnectionData connectNow() {
        Connection connection = pool.getConnection();
        QueryRunner runner = new QueryRunner(connection);
        return new ConnectionData(connection, runner, now() + timeoutToExpireMillis);
    }

    private static long now() {
        return System.currentTimeMillis();
    }

    private record ConnectionData(@NotNull Connection connection, @NotNull QueryRunner runner, long expireEpochMillis) {
        public void close() {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                Unchecked.rethrow(e);
            }
        }

        public boolean isOpen() {
            try {
                return !connection.isClosed();
            } catch (SQLException e) {
                return Unchecked.rethrow(e);
            }
        }
    }
}
