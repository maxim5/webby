package io.spbx.webby.demo;

import com.google.common.flogger.FluentLogger;
import io.spbx.orm.api.Engine;
import io.spbx.webby.Webby;
import io.spbx.webby.app.AppSettings;
import io.spbx.webby.db.kv.DbType;
import io.spbx.webby.db.kv.KeyValueSettings;
import io.spbx.webby.db.sql.SqlSettings;
import io.spbx.webby.demo.templates.JteExample;
import io.spbx.webby.netty.dispatch.NettyBootstrap;
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

    public static @NotNull AppSettings localSettings() {
        AppSettings settings = new AppSettings();
        settings.setDevMode(true);
        settings.setSecurityKey("12345678901234567890123456789012");
        settings.setWebPath(DevPaths.DEMO_WEB);
        settings.setViewPath(DevPaths.DEMO_WEB);
        settings.setUserContentPath(DevPaths.DEMO_HOME + ".data/userdata");
        settings.modelFilter().setPackageOnly("io.spbx.webby");  // because this class is in `io.spbx.webby.demo`
        settings.setProperty("jte.class.directory", JteExample.CLASS_DIR);
        settings.setProperty("jte.view.paths", DevPaths.DEMO_WEB + "jte");
        settings.setProperty("pebble.view.paths", DevPaths.DEMO_WEB + "pebble");
        settings.setProperty("db.mapdb.checksum.enabled", false);
        settings.storageSettings()
            .enableKeyValue(KeyValueSettings.of(DbType.MAP_DB, DevPaths.DEMO_HOME + ".data/mapdb"))
            .enableSql(SqlSettings.inMemoryNotForProduction(Engine.H2));
        return settings;
    }
}