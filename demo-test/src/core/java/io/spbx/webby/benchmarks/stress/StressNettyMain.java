package io.spbx.webby.benchmarks.stress;

import io.spbx.util.testing.TestingBasics;
import io.spbx.webby.Webby;
import io.spbx.webby.app.AppSettings;
import io.spbx.webby.auth.session.DefaultSession;
import io.spbx.webby.auth.user.DefaultUser;
import io.webby.demo.Main;
import io.spbx.webby.netty.dispatch.NettyBootstrap;
import io.spbx.webby.netty.dispatch.http.NettyHttpHandler;
import io.spbx.webby.netty.response.HttpResponseFactory;
import io.spbx.webby.testing.*;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.util.List;

import static io.spbx.webby.benchmarks.stress.ConcurrentStressing.MEDIUM_WAIT;
import static io.spbx.webby.benchmarks.stress.ConcurrentStressing.execWorkers;
import static io.spbx.webby.testing.OkRequests.json;
import static io.spbx.util.base.Unchecked.Runnables.rethrow;

public class StressNettyMain {
    private static final int PORT = 7000;
    private static final OkRequests Ok = OkRequests.ofLocalhost(PORT);

    public static void main(String[] args) throws Exception {
        try (Closeable ignored = bootstrapServer()) {
            new MemoryMonitor().startDaemon();

            HttpWorker.Init init = new HttpWorker.Init(1000_000, new ProgressMonitor(5_000), new Rand());
            execWorkers(MEDIUM_WAIT, List.of(
                HttpWorker.randomListCaller(init, List.of(Ok.get("/"), Ok.post("/user/", json("")))),
                HttpWorker.randomListCaller(init, List.of(Ok.get("/"), Ok.post("/user/", json("")))),
                HttpWorker.randomListCaller(init, List.of(Ok.get("/"), Ok.post("/user/", json("")))),
                HttpWorker.randomListCaller(init, List.of(Ok.get("/"), Ok.get("/user/1"), Ok.get("/user/")))
            ));
        }
    }

    private static @NotNull Closeable bootstrapServer() {
        AppSettings settings = Main.localSettings();
        settings.modelFilter().setCommonPackageOf(DefaultUser.class, DefaultSession.class);
        settings.handlerFilter().setPackageOnly("io.webby.demo");
        settings.storageSettings()
                .enableKeyValue(TestingStorage.KEY_VALUE_DEFAULT)
                .enableSql(TestingProps.propsSqlSettings());
        settings.setProfileMode(false);
        settings.setProperty("db.sql.connection.expiration.millis", 10_000);
        // H2 default max connections = 10!
        // https://stackoverflow.com/questions/27836756/maximum-number-of-connections-possible-with-h2-in-server-mode
        // settings.setProperty("netty.worker.group.threads", 8);
        NettyBootstrap bootstrap = Webby.nettyBootstrap(settings, TestingModules.PERSISTENT_DB_CLEANER_MODULE);

        Thread server = new Thread(rethrow(() -> {
            Configurator.setLevel(NettyHttpHandler.class.getName(), Level.WARN);
            Configurator.setLevel(HttpResponseFactory.class.getName(), Level.WARN);
            bootstrap.runLocally(PORT);
        }));
        server.start();
        TestingBasics.waitFor(300);

        return server::interrupt;
    }
}
