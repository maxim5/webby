package io.spbx.webby.demo;

import com.google.common.flogger.FluentLogger;
import io.spbx.orm.api.Engine;
import io.spbx.webby.Webby;
import io.spbx.webby.app.AppSettings;
import io.spbx.webby.app.Settings.RunMode;
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
        AppSettings settings = AppSettings.fromProperties(DevPaths.DEMO_RESOURCES.resolve("app.properties"));
        settings.setRunMode(RunMode.DEV);
        settings.setSecurityKey("12345678901234567890123456789012");
        settings.setWebPath(DevPaths.DEMO_WEB);
        settings.setViewPath(DevPaths.DEMO_WEB);
        settings.setUserContentPath(DevPaths.DEMO_HOME.resolve(".data/userdata"));
        settings.modelFilter().setPackageOnly("io.spbx.webby");  // because this class is in `io.spbx.webby.demo`
        settings.setPath("jte.class.directory", JteExample.CLASS_DIR);
        settings.setPath("jte.view.paths", DevPaths.DEMO_WEB.resolve("jte"));
        settings.setPath("pebble.view.paths", DevPaths.DEMO_WEB.resolve("pebble"));
        settings.setBool("db.mapdb.checksum.enabled", false);
        settings.storageSettings()
            .enableKeyValue(KeyValueSettings.of(DbType.MAP_DB, DevPaths.DEMO_HOME.resolve(".data/mapdb")))
            .enableSql(SqlSettings.inMemoryForDevOnly(Engine.H2));
        return settings;
    }
}
