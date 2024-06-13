package io.webby.db;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.flogger.FluentLogger;
import com.google.inject.Inject;
import io.webby.app.Settings;
import io.webby.app.GuiceCompleteEvent;
import io.webby.db.sql.SqlReadyEvent;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

public class DbReadiness {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    private final EventBus eventBus;
    private final AtomicBoolean sqlReady;

    @Inject
    public DbReadiness(@NotNull Settings settings, @NotNull EventBus eventBus) {
        sqlReady = new AtomicBoolean(!settings.storageSettings().isSqlEnabled());
        this.eventBus = eventBus;
        eventBus.register(this);
    }

    @Subscribe
    public void guiceComplete(@NotNull GuiceCompleteEvent event) {
        if (sqlReady.get()) {
            postEventIfAllReady();
        }
    }

    @Subscribe
    public void sqlReady(@NotNull SqlReadyEvent event) {
        if (sqlReady.compareAndSet(false, true)) {
            postEventIfAllReady();
        }
    }

    private void postEventIfAllReady() {
        log.at(Level.FINE).log("Db is ready");
        eventBus.unregister(this);
        eventBus.post(new DbReadyEvent());
    }
}
