package io.webby.db.cache;

import com.google.common.flogger.FluentLogger;
import com.google.inject.Inject;
import io.webby.common.Lifetime;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;

public class BackgroundCacheCleaner {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    // FIX[minor]: make options for these
    private static final long SOFT_CLEAN_TIMEOUT_SEC = TimeUnit.HOURS.toSeconds(1);
    private static final long HARD_CLEAN_TIMEOUT_MILLIS = TimeUnit.HOURS.toMillis(12);

    private final ConcurrentMap<String, ManagedPersistent> caches = new ConcurrentHashMap<>();
    private final AtomicBoolean cleanInProgress = new AtomicBoolean();
    private final AtomicLong lastHardClean = new AtomicLong(System.currentTimeMillis());

    @Inject
    public BackgroundCacheCleaner(@NotNull Lifetime lifetime) {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(this::runClean, SOFT_CLEAN_TIMEOUT_SEC, SOFT_CLEAN_TIMEOUT_SEC, TimeUnit.SECONDS);
        lifetime.onTerminate(executor::shutdown);
    }

    public void register(@NotNull String name, @NotNull ManagedPersistent cache) {
        assert !caches.containsKey(name) : "Cache already exists: " + name;
        caches.put(name, cache);
    }

    public boolean isCleanInProgress() {
        return cleanInProgress.get();
    }

    private void runClean() {
        if (!cleanInProgress.compareAndSet(false, true)) {
            log.at(Level.WARNING).log("Cleaning is already in progress. Return immediately");
            return;
        }

        long sinceLastHardClean = System.currentTimeMillis() - lastHardClean.get();
        FlushMode mode = sinceLastHardClean < HARD_CLEAN_TIMEOUT_MILLIS ? FlushMode.INCREMENTAL : FlushMode.FULL_COMPACT;

        try {
            for (Map.Entry<String, ManagedPersistent> entry : caches.entrySet()) {
                String name = entry.getKey();
                ManagedPersistent cache = entry.getValue();
                log.at(Level.INFO).log("Flushing cache=`%s` mode=`%s`...", name, mode);
                try {
                    cache.flush(mode);
                } catch (Throwable e) {
                    log.at(Level.SEVERE).withCause(e).log("Flush failed: cache=`%s` mode=`%s`", name, mode);
                } finally {
                    log.at(Level.INFO).log("Flush completed: cache=`%s` mode=`%s`", name, mode);
                }
            }
        } finally {
            cleanInProgress.set(false);
            if (mode == FlushMode.FULL_COMPACT) {
                lastHardClean.set(System.currentTimeMillis());
            }
        }
    }
}
