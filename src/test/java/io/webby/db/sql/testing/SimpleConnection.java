package io.webby.db.sql.testing;

import com.google.common.flogger.FluentLogger;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class SimpleConnection extends ConnectionAdapter {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    private boolean closed = false;
    private final Set<Long> access = ConcurrentHashMap.newKeySet();

    public SimpleConnection() {
        markAccess("SimpleConnection()");
    }

    @Override
    public boolean isClosed() {
        markAccess("SimpleConnection.isClosed()");
        return closed;
    }

    @Override
    public void close() {
        markAccess("SimpleConnection.close()");
        assertFalse(closed, "SimpleConnection is already closed");
        closed = true;
    }

    @Override
    public String toString() {
        return "SimpleConnection{closed=%s, access=%s}".formatted(closed, access);
    }

    public boolean isSingleThreadAccess() {
        return access.size() == 1;
    }

    public @NotNull Set<Long> access() {
        return access;
    }

    private void markAccess(@NotNull String message) {
        long threadId = Thread.currentThread().getId();
        log.at(Level.FINE).log("[%d] %s", message, threadId);
        access.add(threadId);
    }
}
