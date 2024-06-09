package io.webby.testing.ext;

import io.webby.db.sql.SqlSettings;
import org.jetbrains.annotations.NotNull;

public interface EmbeddedDb extends AutoCloseable {
    @NotNull EmbeddedDb startup();

    void shutdown();

    @Override
    default void close() {
        shutdown();
    }

    static @NotNull EmbeddedDb getDb(@NotNull SqlSettings settings) {
        return switch (settings.engine()) {
            case MariaDB -> new EmbeddedMariaDbServer(settings);
            case SQLite, H2, MySQL -> new EmbeddedDb() {
                @Override
                public @NotNull EmbeddedDb startup() {
                    return this;
                }
                @Override
                public void shutdown() {}
            };
            default -> throw new UnsupportedOperationException("Engine does not support embedded db: " + settings.url());
        };
    }
}
