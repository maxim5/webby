package io.webby.perf;

import com.google.inject.Injector;
import io.webby.Webby;
import io.webby.app.AppSettings;
import io.webby.auth.session.Session;
import io.webby.auth.user.DefaultUser;
import io.webby.auth.user.UserAccess;
import io.webby.auth.user.UserTable;
import io.webby.db.sql.ConnectionPool;
import io.webby.db.sql.SqlSettings;
import io.webby.perf.TableWorker.Init;
import io.webby.testing.Testing;

import java.sql.Connection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class StressUserTableMain {
    public static void main(String[] args) throws Exception {
        AppSettings settings = Testing.defaultAppSettings();
        settings.modelFilter().setCommonPackageOf(DefaultUser.class, Session.class);
        settings.storageSettings().enableSqlStorage(SqlSettings.inMemoryNotForProduction());
        Injector injector = Webby.initGuice(settings);

        ConnectionPool pool = injector.getInstance(ConnectionPool.class);
        try (Connection connection = pool.getConnection()) {
            connection.createStatement().executeUpdate("""
                CREATE TABLE user (
                    user_id INTEGER PRIMARY KEY,
                    access_level INTEGER
                )
            """);
        }
        UserTable userTable = new UserTable(pool.getConnection());
        RandomLongGenerator generator = new RandomLongGenerator(10_000);
        Init<Long, DefaultUser> init = new Init<>(3_000_000, userTable, generator);

        List<Runnable> workers = List.of(
            TableWorker.inserter(init, id -> new DefaultUser(id, UserAccess.Simple)),
            TableWorker.updater(init, id -> new DefaultUser(id, UserAccess.Admin)),
            TableWorker.deleter(init),
            TableWorker.reader(init)
        );
        ExecutorService executor = Executors.newFixedThreadPool(workers.size());
        workers.forEach(executor::execute);

        new MemoryMonitor(1000).startDaemon();

        executor.shutdown();
        executor.awaitTermination(100, TimeUnit.MILLISECONDS);
    }
}
