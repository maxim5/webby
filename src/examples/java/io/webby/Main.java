package io.webby;

import com.google.common.flogger.FluentLogger;
import com.google.inject.Injector;
import io.webby.app.AppSettings;
import io.webby.netty.NettyBootstrap;
import io.webby.templates.JteExample;

import java.io.FileInputStream;
import java.util.logging.Level;
import java.util.logging.LogManager;

public class Main {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    public static void main(String[] args) throws Exception {
        LogManager.getLogManager().readConfiguration(new FileInputStream("out/examples/resources/logging.properties"));
        runNetty(8888);
    }

    private static void runNetty(int port) throws InterruptedException {
        log.at(Level.INFO).log("Bootstrapping server");

        AppSettings settings = new AppSettings();
        settings.setWebPath("out/examples/resources/web/");
        settings.setViewPath("out/examples/resources/web/");
        settings.setDevMode(true);
        settings.setProperty("jte.class.directory", JteExample.CLASS_DIR);
        settings.setProperty("jte.view.paths", "out/examples/resources/web/jte");
        settings.setProperty("pebble.view.paths", "out/examples/resources/web/pebble");

        Injector injector = Webby.initGuice(settings);
        NettyBootstrap startup = injector.getInstance(NettyBootstrap.class);
        startup.runLocally(port);
    }
}
