package io.webby.db.event;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;

public interface Persistable extends Closeable, Flushable {
    @Override
    default void flush() throws IOException {
        forceFlush();
    }

    void forceFlush() throws IOException;
}
