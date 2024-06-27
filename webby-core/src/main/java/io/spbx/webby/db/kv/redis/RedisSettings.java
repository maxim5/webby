package io.spbx.webby.db.kv.redis;

import com.google.inject.Inject;
import io.spbx.util.props.PropertyMap;
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
    public RedisSettings(@NotNull PropertyMap properties) {
        host = properties.get("db.redis.host", "localhost");
        port = properties.getInt("db.redis.port", 6379);
        user = properties.getOrNull("db.redis.user");
        password = properties.getOrNull("db.redis.password");
        database = properties.getInt("db.redis.database", 0);
        ssl = properties.getBool("db.redis.ssl", false);
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
