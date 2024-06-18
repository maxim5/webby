package io.webby.benchmarks.stress;

import com.google.inject.Injector;
import io.spbx.orm.api.Connector;
import io.webby.Webby;
import io.webby.app.AppSettings;
import io.webby.auth.session.DefaultSession;
import io.webby.auth.user.DefaultUser;
import io.webby.auth.user.UserAccess;
import io.webby.auth.user.UserTable;
import io.webby.benchmarks.stress.TableWorker.Init;
import io.webby.db.sql.TableManager;
import io.webby.db.sql.ThreadLocalConnector;
import io.webby.testing.Testing;
import io.webby.testing.TestingModules;
import io.webby.testing.TestingProps;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.List;
import java.util.function.Consumer;

import static io.webby.benchmarks.stress.ConcurrentStressing.MEDIUM_WAIT;
import static io.webby.benchmarks.stress.ConcurrentStressing.execWorkers;

public class StressUserTableMain {
    public static void main(String[] args) throws Exception {
        Connector connector = initConnector();
        new MemoryMonitor().startDaemon();

        Init<Integer, DefaultUser> init = initWorkers(connector);
        execWorkers(MEDIUM_WAIT, List.of(
            TableWorker.inserter(init, id -> DefaultUser.newUser(id, Instant.now(), UserAccess.Simple)),
            TableWorker.updater(init, id -> DefaultUser.newUser(id, Instant.now(), UserAccess.SuperAdmin)),
            TableWorker.deleter(init),
            TableWorker.reader(init),
            TableWorker.scanner(init.withSteps(0.00001))
        ));
    }

    private static @NotNull Init<Integer, DefaultUser> initWorkers(@NotNull Connector connector) {
        UserTable userTable = new UserTable(connector);
        ProgressMonitor progress = new ProgressMonitor();
        RandomIntGenerator generator = new RandomIntGenerator(100_000);
        Consumer<Integer> randomConnectionCleaner = step -> {
            if (generator.random() < 1e-4) {
                ThreadLocalConnector.cleanupIfNecessary();
            }
        };
        return new Init<>(10_000_000, progress, userTable, generator, randomConnectionCleaner);
    }

    private static @NotNull Connector initConnector() {
        AppSettings settings = Testing.defaultAppSettings();
        settings.modelFilter().setCommonPackageOf(DefaultUser.class, DefaultSession.class);
        settings.storageSettings().enableSql(TestingProps.propsSqlSettings());
        settings.setProperty("db.sql.connection.expiration.millis", 10_000);
        Injector injector = Webby.getReady(settings, TestingModules.PERSISTENT_DB_CLEANER_MODULE);
        return injector.getInstance(TableManager.class).connector();
    }
}
