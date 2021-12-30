package io.webby.db.kv;

public interface InMemoryDb<K, V> extends KeyValueDb<K, V> {
    @Override
    default void flush() {
        // unsupported
    }

    @Override
    default void forceFlush() {
        // unsupported
    }

    @Override
    default void close() {
        // nothing to do by default
    }
}
