package io.spbx.webby.netty;

import com.google.common.flogger.FluentLogger;
import io.spbx.util.testing.TestingBasics;
import io.spbx.webby.Webby;
import io.spbx.webby.app.AppSettings;
import io.spbx.webby.netty.dispatch.NettyBootstrap;
import io.spbx.webby.testing.TestingModules;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.logging.Level;

public class StandaloneNettyExtension implements BeforeAllCallback, AfterAllCallback {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();
    public static final int DEFAULT_PORT = 9999;

    private final NettyBootstrap bootstrap;
    private final Thread serverThread;
    private final int port;

    public StandaloneNettyExtension(@NotNull AppSettings settings, int port) {
        bootstrap = Webby.nettyBootstrap(settings, TestingModules.PERSISTENT_DB_CLEANER_MODULE);
        serverThread = new Thread(() -> {
            try {
                bootstrap.runLocally(port);
            } catch (InterruptedException e) {
                log.at(Level.FINE).withCause(e).log("Server thread interrupted");
            }
        });
        this.port = port;
    }

    public StandaloneNettyExtension(@NotNull AppSettings settings) {
        this(settings, DEFAULT_PORT);
    }

    @Override
    public void beforeAll(ExtensionContext context) {
        log.at(Level.INFO).log("Bootstrapping standalone server...");
        serverThread.start();
        TestingBasics.waitFor(300);
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
