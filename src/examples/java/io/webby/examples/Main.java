package io.webby.examples;

import com.google.common.flogger.FluentLogger;
import io.webby.Webby;
import io.webby.app.AppSettings;
import io.webby.db.kv.StorageType;
import io.webby.db.sql.SqlSettings;
import io.webby.examples.templates.JteExample;
import io.webby.netty.NettyBootstrap;
import io.webby.orm.api.Engine;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

public class Main {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    public static void main(String[] args) throws Exception {
        runNetty(8888);
    }

    public static void runNetty(int port) throws InterruptedException {
        log.at(Level.INFO).log("Bootstrapping server");
        AppSettings settings = localSettings();
        NettyBootstrap bootstrap = Webby.nettyBootstrap(settings);
        bootstrap.runLocally(port);
    }

    @NotNull
    public static AppSettings localSettings() {
        AppSettings settings = new AppSettings();
        settings.setDevMode(true);
        settings.setSecurityKey("12345678901234567890123456789012");
        settings.setWebPath("out/examples/resources/web");
        settings.setViewPath("out/examples/resources/web");
        settings.modelFilter().setPackageOnly("io.webby");  // because this class is in `io.webby.examples`
        settings.setProperty("jte.class.directory", JteExample.CLASS_DIR);
        settings.setProperty("jte.view.paths", "out/examples/resources/web/jte");
        settings.setProperty("pebble.view.paths", "out/examples/resources/web/pebble");
        settings.setProperty("db.mapdb.checksum.enabled", false);
        settings.storageSettings().enableKeyValueStorage(StorageType.MAP_DB).setKeyValueStoragePath(".data/mapdb")
                .enableSqlStorage(SqlSettings.inMemoryNotForProduction(Engine.H2));
        return settings;
    }
}
