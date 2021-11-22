package io.webby.db.sql;

import io.webby.orm.api.Connector;
import io.webby.orm.api.QueryRunner;
import io.webby.util.base.Rethrow;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;

public class ThreadLocalConnector implements Connector {
    private static final ThreadLocal<ConnectionData> local = new ThreadLocal<>();

    private final ConnectionPool pool;
    private final long keepOpenTimeout;

    public ThreadLocalConnector(@NotNull ConnectionPool pool, long keepOpenTimeout) {
        this.pool = pool;
        this.keepOpenTimeout = keepOpenTimeout;
    }

    @Override
    public @NotNull Connection connection() {
        ConnectionData data = local.get();
        if (data != null) {
            return data.connection;
        }
        local.set(connectNow());
        return local.get().connection;
    }

    @Override
    public @NotNull QueryRunner runner() {
        ConnectionData data = local.get();
        if (data != null) {
            return data.runner;
        }
        local.set(connectNow());
        return local.get().runner;
    }

    public void refreshIfNecessary() {
        ConnectionData data = local.get();
        if (data != null && now() - data.openedTimestamp > data.keepOpenTimeout) {
            data.close();
            local.set(connectNow());
        }
    }

    public static void cleanupIfNecessary() {
        ConnectionData data = local.get();
        if (data != null && now() - data.openedTimestamp > data.keepOpenTimeout) {
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
        long openedTimestamp = now();
        return new ConnectionData(connection, runner, openedTimestamp, keepOpenTimeout);
    }

    private static long now() {
        return System.currentTimeMillis();
    }

    private record ConnectionData(@NotNull Connection connection, @NotNull QueryRunner runner,
                                  long openedTimestamp, long keepOpenTimeout) {
        public void close() {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                Rethrow.rethrow(e);
            }
        }

        @SuppressWarnings("deprecation")
        @Override
        protected void finalize() {
            close();
        }
    }
}
