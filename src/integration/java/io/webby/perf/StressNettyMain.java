package io.webby.perf;

import io.webby.Webby;
import io.webby.app.AppSettings;
import io.webby.db.kv.StorageType;
import io.webby.db.sql.SqlSettings;
import io.webby.examples.Main;
import io.webby.netty.NettyBootstrap;
import io.webby.netty.NettyHttpHandler;
import io.webby.orm.api.Engine;
import io.webby.perf.HttpWorker.Init;
import io.webby.testing.TestingUtil;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.util.List;

import static io.webby.perf.ConcurrentStressing.MEDIUM_WAIT;
import static io.webby.perf.ConcurrentStressing.execWorkers;
import static io.webby.util.base.Rethrow.Runnables.rethrow;

public class StressNettyMain {
    private static final int PORT = 7000;
    private static final OkRequests Ok = OkRequests.of(PORT);

    public static void main(String[] args) throws Exception {
        try (Closeable ignored = bootstrapServer()) {
            new MemoryMonitor().startDaemon();

            Init init = new Init(1000_000, new ProgressMonitor(5_000), new Rand());
            execWorkers(MEDIUM_WAIT, List.of(
                HttpWorker.randomListCaller(init, List.of(Ok.get("/"), Ok.postJson("/user/", ""))),
                HttpWorker.randomListCaller(init, List.of(Ok.get("/"), Ok.postJson("/user/", ""))),
                HttpWorker.randomListCaller(init, List.of(Ok.get("/"), Ok.postJson("/user/", ""))),
                HttpWorker.randomListCaller(init, List.of(Ok.get("/"), Ok.get("/user/1"), Ok.get("/user/")))
            ));
        }
    }

    private static @NotNull Closeable bootstrapServer() {
        AppSettings settings = Main.localSettings();
        settings.storageSettings().enableKeyValueStorage(StorageType.SQL_DB)
                .enableSqlStorage(SqlSettings.inMemoryNotForProduction(Engine.H2));
        settings.setProfileMode(false);
        settings.handlerFilter().setPackageOnly("io.webby.examples");
        settings.setProperty("db.sql.connection.expiration.millis", 1_000);
        // H2 default max connections = 10!
        // https://stackoverflow.com/questions/27836756/maximum-number-of-connections-possible-with-h2-in-server-mode
        settings.setProperty("netty.worker.group.threads", 8);
        NettyBootstrap bootstrap = Webby.nettyBootstrap(settings);

        Thread server = new Thread(rethrow(() -> {
            Configurator.setLevel(NettyHttpHandler.class.getName(), Level.WARN);
            bootstrap.runLocally(PORT);
        }));
        server.start();
        TestingUtil.waitFor(300);

        return server::interrupt;
    }
}
