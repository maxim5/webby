package io.webby.testing.ext;

import com.google.common.flogger.FluentLogger;
import io.webby.util.io.EasyNet;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import redis.embedded.RedisServer;

import java.util.logging.Level;

public class EmbeddedRedisExtension implements BeforeAllCallback, AfterAllCallback {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();
    public static final int DEFAULT_PORT = 6379;

    private final RedisServer redisServer;

    public EmbeddedRedisExtension(int port) {
        // See
        // https://github.com/kstyrc/embedded-redis/issues/52
        // https://github.com/kstyrc/embedded-redis/issues/77
        // https://stackoverflow.com/questions/30233543/error-when-open-redis-server
        redisServer = RedisServer.builder().port(port).setting("maxmemory 512M").build();
    }

    public static @NotNull EmbeddedRedisExtension of(int port) {
        return new EmbeddedRedisExtension(port);
    }

    public static @NotNull EmbeddedRedisExtension ofDefaultPort() {
        return of(DEFAULT_PORT);
    }

    public static @NotNull EmbeddedRedisExtension ofAnyAvailablePort() {
        return of(EasyNet.nextAvailablePort());
    }

    @Override
    public void beforeAll(ExtensionContext context) {
        log.at(Level.INFO).log("Starting embedded Redis server at %d...", port());
        redisServer.start();
    }

    @Override
    public void afterAll(ExtensionContext context) {
        log.at(Level.INFO).log("Stopping embedded Redis...");
        redisServer.stop();
    }

    public int port() {
        return redisServer.ports().getFirst();
    }
}
