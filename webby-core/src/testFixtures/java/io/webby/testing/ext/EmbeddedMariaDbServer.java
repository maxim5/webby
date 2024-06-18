package io.webby.testing.ext;

import ch.vorburger.exec.ManagedProcessException;
import ch.vorburger.mariadb4j.DB;
import ch.vorburger.mariadb4j.DBConfiguration;
import ch.vorburger.mariadb4j.DBConfigurationBuilder;
import com.google.common.flogger.FluentLogger;
import io.webby.db.sql.SqlSettings;
import io.webby.util.time.TimeIt;
import io.webby.util.base.Unchecked;
import io.webby.util.lazy.AtomicLazyInit;
import io.webby.util.lazy.LazyInit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Level;

import static io.webby.util.base.EasyPrimitives.requirePositive;
import static java.util.Objects.requireNonNull;

public class EmbeddedMariaDbServer implements EmbeddedDb {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    private final SqlSettings settings;
    private final LazyInit<Data> data = AtomicLazyInit.createUninitialized();

    public EmbeddedMariaDbServer(@NotNull SqlSettings settings) {
        this.settings = settings;
    }

    @Override
    public @NotNull EmbeddedDb startup() {
        int port = requirePositive(settings.port());
        String dbName = requireNonNull(settings.databaseName());
        DB db = startDb(port, dbName);
        data.initializeOrDie(new Data(db, port, dbName));
        return this;
    }

    @Override
    public void shutdown() {
        try {
            data.getOrDie().db().stop();
            log.at(Level.INFO).log("Shutdown embedded MariaDb: %s", data.getOrDie());
        } catch (ManagedProcessException e) {
            Unchecked.rethrow(e);
        }
    }

    public static @NotNull DB startDb(int port, @NotNull String dbName) {
        return TimeIt.timeIt(() -> {
            try {
                DBConfiguration config = DBConfigurationBuilder.newBuilder().setPort(port).build();
                DB db = DB.newEmbeddedDB(config);
                db.start();
                db.createDB(dbName);
                return db;
            } catch (ManagedProcessException e) {
                return Unchecked.rethrow(e);
            }
        }).onDone((db, millis) -> log.at(Level.INFO).log("Started embedded MariaDb in %d ms: %s",
                                                         millis, Data.describe(db, dbName)));
    }

    private record Data(@NotNull DB db, int port, @NotNull String dbName) {
        @Override
        public @NotNull String toString() {
            return db.getConfiguration().getURL(dbName);
        }

        private static String describe(@NotNull DB db, @Nullable String dbName) {
            return db.getConfiguration().getURL(dbName != null ? dbName : "?");
        }
    }
}
