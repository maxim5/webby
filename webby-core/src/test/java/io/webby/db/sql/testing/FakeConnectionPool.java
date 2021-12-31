package io.webby.db.sql.testing;

import io.webby.db.sql.ConnectionPool;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.util.List;
import java.util.Vector;
import java.util.function.Supplier;

public class FakeConnectionPool<C extends Connection> extends ConnectionPool {
    private final Supplier<C> creator;
    private final List<C> connections = new Vector<>();  // note: concurrent

    public FakeConnectionPool(@NotNull Supplier<C> creator) {
        this.creator = creator;
    }

    @Override
    public @NotNull Connection getConnection() {
        C adapter = creator.get();
        connections.add(adapter);
        return adapter;
    }

    @Override
    public boolean isRunning() {
        return true;
    }

    public @NotNull List<C> connections() {
        return connections;
    }
}
