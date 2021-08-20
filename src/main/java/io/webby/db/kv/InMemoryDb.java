package io.webby.db.kv;

public interface InMemoryDb<K, V> extends KeyValueDb<K, V> {
    @Override
    default void flush() {}

    @Override
    default void forceFlush() {}

    @Override
    default void close() {}
}
