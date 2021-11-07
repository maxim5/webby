package io.webby.db.kv;

import com.google.common.flogger.FluentLogger;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import redis.embedded.RedisServer;

import java.util.logging.Level;

public class EmbeddedRedisExtension implements BeforeAllCallback, AfterAllCallback {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();
    public static final int PORT = 6379;

    // See
    // https://github.com/kstyrc/embedded-redis/issues/52
    // https://stackoverflow.com/questions/30233543/error-when-open-redis-server
    private final RedisServer redisServer = RedisServer.builder().port(PORT).setting("maxheap 512M").build();

    @Override
    public void beforeAll(ExtensionContext context) {
        log.at(Level.INFO).log("Starting embedded Redis server at %d...", PORT);
        redisServer.start();
    }

    @Override
    public void afterAll(ExtensionContext context) {
        log.at(Level.INFO).log("Stopping embedded Redis...");
        redisServer.stop();
    }
}
