package io.spbx.webby.benchmarks.stress;

import com.google.inject.Injector;
import io.spbx.orm.api.Connector;
import io.spbx.webby.Webby;
import io.spbx.webby.app.AppSettings;
import io.spbx.webby.auth.session.DefaultSession;
import io.spbx.webby.auth.user.DefaultUser;
import io.spbx.webby.auth.user.UserAccess;
import io.spbx.webby.auth.user.UserTable;
import io.spbx.webby.benchmarks.stress.TableWorker.Init;
import io.spbx.webby.db.sql.TableManager;
import io.spbx.webby.db.sql.ThreadLocalConnector;
import io.spbx.webby.testing.Testing;
import io.spbx.webby.testing.TestingModules;
import io.spbx.webby.testing.TestingProps;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.List;
import java.util.function.Consumer;

import static io.spbx.webby.benchmarks.stress.ConcurrentStressing.MEDIUM_WAIT;
import static io.spbx.webby.benchmarks.stress.ConcurrentStressing.execWorkers;

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
        settings.setInt("db.sql.connection.expiration.millis", 10_000);
        Injector injector = Webby.getReady(settings, TestingModules.PERSISTENT_DB_CLEANER_MODULE);
        return injector.getInstance(TableManager.class).connector();
    }
}
