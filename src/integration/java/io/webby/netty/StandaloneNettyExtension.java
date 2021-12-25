package io.webby.netty;

import com.google.common.flogger.FluentLogger;
import io.webby.Webby;
import io.webby.app.AppSettings;
import io.webby.testing.TestingModules;
import io.webby.testing.TestingUtil;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.logging.Level;

import static io.webby.util.base.Rethrow.Runnables.rethrow;

public class StandaloneNettyExtension implements BeforeAllCallback, AfterAllCallback {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();
    public static final int DEFAULT_PORT = 9999;

    private final NettyBootstrap bootstrap;
    private final Thread serverThread;
    private final int port;

    public StandaloneNettyExtension(@NotNull AppSettings settings, int port) {
        bootstrap = Webby.nettyBootstrap(settings, TestingModules.PERSISTENT_DB_CLEANER_MODULE);
        serverThread = new Thread(rethrow(() -> bootstrap.runLocally(port)));
        this.port = port;
    }

    public StandaloneNettyExtension(@NotNull AppSettings settings) {
        this(settings, DEFAULT_PORT);
    }

    @Override
    public void beforeAll(ExtensionContext context) {
        log.at(Level.INFO).log("Bootstrapping standalone server...");
        serverThread.start();
        TestingUtil.waitFor(300);
    }

    @Override
    public void afterAll(ExtensionContext context) {
        log.at(Level.INFO).log("Sending interrupt to the standalone server...");
        serverThread.interrupt();
    }

    public int port() {
        return port;
    }
}
