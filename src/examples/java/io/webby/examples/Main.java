package io.webby.examples;

import com.google.common.flogger.FluentLogger;
import com.google.inject.Injector;
import io.webby.Webby;
import io.webby.app.AppSettings;
import io.webby.db.kv.StorageType;
import io.webby.db.sql.SqlSettings;
import io.webby.examples.templates.JteExample;
import io.webby.netty.NettyBootstrap;

import java.util.logging.Level;

public class Main {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    public static void main(String[] args) throws Exception {
        runNetty(8888);
    }

    private static void runNetty(int port) throws InterruptedException {
        log.at(Level.INFO).log("Bootstrapping server");

        AppSettings settings = new AppSettings();
        settings.setSecurityKey("12345678901234567890123456789012");
        settings.setWebPath("out/examples/resources/web");
        settings.setViewPath("out/examples/resources/web");
        settings.setDevMode(true);
        settings.setProperty("jte.class.directory", JteExample.CLASS_DIR);
        settings.setProperty("jte.view.paths", "out/examples/resources/web/jte");
        settings.setProperty("pebble.view.paths", "out/examples/resources/web/pebble");
        settings.setProperty("db.mapdb.checksum.enabled", false);
        settings.storageSettings().enableKeyValueStorage(StorageType.MAP_DB).setKeyValueStoragePath(".data/mapdb")
                .enableSqlStorage(SqlSettings.inMemoryNotForProduction());

        Injector injector = Webby.initGuice(settings);
        NettyBootstrap startup = injector.getInstance(NettyBootstrap.class);
        startup.runLocally(port);
    }
}
