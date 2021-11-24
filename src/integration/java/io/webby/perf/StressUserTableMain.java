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
import io.webby.orm.api.Engine;
import io.webby.perf.TableWorker.Init;
import io.webby.testing.Testing;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

// Possible URLs:
// SqlSettings.jdbcUrl(Engine.H2, "file:./.data/temp.h2")
// SqlSettings.jdbcUrl(SqlSettings.SQLITE, "file:.data/temp.sqlite.db?mode=memory&cache=shared")
public class StressUserTableMain {
    private static final SqlSettings SQL_SETTINGS = SqlSettings.inMemoryNotForProduction(Engine.H2);

    public static void main(String[] args) throws Exception {
        AppSettings settings = Testing.defaultAppSettings();
        settings.modelFilter().setCommonPackageOf(DefaultUser.class, Session.class);
        settings.storageSettings().enableSqlStorage(SQL_SETTINGS);
        settings.setProperty("db.sql.connection.keep.open.millis", 10_000);
        Injector injector = Webby.initGuice(settings);

        TableManager tableManager = injector.getInstance(TableManager.class);
        tableManager.connector().runner().runMultiUpdate("""
            CREATE TABLE IF NOT EXISTS user (
                user_id INTEGER PRIMARY KEY,
                access_level INTEGER
            )
        """);

        UserTable userTable = new UserTable(tableManager.connector());
        ProgressMonitor progress = new ProgressMonitor();
        RandomLongGenerator generator = new RandomLongGenerator(100_000);
        Consumer<Integer> randomConnectionCleaner = step -> {
            if (generator.random() < 1e-4) {
                ThreadLocalConnector.cleanupIfNecessary();
            }
        };
        Init<Long, DefaultUser> init = new Init<>(10_000_000, progress, userTable, generator, randomConnectionCleaner);

        List<Worker> workers = List.of(
            TableWorker.inserter(init, id -> new DefaultUser(id, UserAccess.Simple)),
            TableWorker.updater(init, id -> new DefaultUser(id, UserAccess.Admin)),
            TableWorker.deleter(init),
            TableWorker.reader(init),
            TableWorker.scanner(init.withSteps(1000))
        );
        ExecutorService executor = Executors.newFixedThreadPool(workers.size());
        workers.forEach(executor::execute);

        new MemoryMonitor(1000).startDaemon();

        executor.shutdown();
        executor.awaitTermination(100, TimeUnit.MILLISECONDS);
    }
}
