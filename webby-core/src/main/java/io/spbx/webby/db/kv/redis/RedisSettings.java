package io.spbx.webby.db.kv.redis;

import com.google.inject.Inject;
import io.spbx.webby.app.Settings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RedisSettings {
    private final String host;
    private final int port;
    private final String user;
    private final String password;
    private final int database;
    private final boolean ssl;

    @Inject
    public RedisSettings(@NotNull Settings settings) {
        host = settings.getProperty("db.redis.host", "localhost");
        port = settings.getIntProperty("db.redis.port", 6379);
        user = settings.getProperty("db.redis.user");
        password = settings.getProperty("db.redis.password");
        database = settings.getIntProperty("db.redis.database", 0);
        ssl = settings.getBoolProperty("db.redis.ssl", false);
    }

    public @NotNull String host() {
        return host;
    }

    public int port() {
        return port;
    }

    public @Nullable String user() {
        return user;
    }

    public @Nullable String password() {
        return password;
    }

    public int database() {
        return database;
    }

    public boolean ssl() {
        return ssl;
    }
}
