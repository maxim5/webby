package io.webby.perf;

import com.google.inject.Injector;
import io.webby.Webby;
import io.webby.app.AppSettings;
import io.webby.auth.session.Session;
import io.webby.auth.user.DefaultUser;
import io.webby.auth.user.UserAccess;
import io.webby.auth.user.UserTable;
import io.webby.db.sql.SqlSettings;
import io.webby.db.sql.TableManager;
import io.webby.db.sql.ThreadLocalConnector;
import io.webby.orm.api.Connector;
import io.webby.orm.api.Engine;
import io.webby.perf.TableWorker.Init;
import io.webby.testing.Testing;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

import static io.webby.perf.ConcurrentStressing.MEDIUM_WAIT;
import static io.webby.perf.ConcurrentStressing.execWorkers;

public class StressUserTableMain {
    public static void main(String[] args) throws Exception {
        Connector connector = initConnector();
        new MemoryMonitor().startDaemon();

        Init<Long, DefaultUser> init = initWorkers(connector);
        execWorkers(MEDIUM_WAIT, List.of(
            TableWorker.inserter(init, id -> new DefaultUser(id, UserAccess.Simple)),
            TableWorker.updater(init, id -> new DefaultUser(id, UserAccess.Admin)),
            TableWorker.deleter(init),
            TableWorker.reader(init),
            TableWorker.scanner(init.withSteps(0.00001))
        ));
    }

    private static @NotNull Init<Long, DefaultUser> initWorkers(@NotNull Connector connector) {
        UserTable userTable = new UserTable(connector);
        ProgressMonitor progress = new ProgressMonitor();
        RandomLongGenerator generator = new RandomLongGenerator(100_000);
        Consumer<Integer> randomConnectionCleaner = step -> {
            if (generator.random() < 1e-4) {
                ThreadLocalConnector.cleanupIfNecessary();
            }
        };
        return new Init<>(10_000_000, progress, userTable, generator, randomConnectionCleaner);
    }

    // More SQL URLs:
    // SqlSettings.jdbcUrl(Engine.H2, "file:./.data/temp.h2")
    // SqlSettings.jdbcUrl(SqlSettings.SQLITE, "file:.data/temp.sqlite.db?mode=memory&cache=shared")
    private static @NotNull Connector initConnector() {
        AppSettings settings = Testing.defaultAppSettings();
        settings.modelFilter().setCommonPackageOf(DefaultUser.class, Session.class);
        settings.storageSettings().enableSqlStorage(SqlSettings.inMemoryNotForProduction(Engine.H2));
        settings.setProperty("db.sql.connection.expiration.millis", 10_000);
        Injector injector = Webby.getReady(settings);
        return injector.getInstance(TableManager.class).connector();
    }
}
