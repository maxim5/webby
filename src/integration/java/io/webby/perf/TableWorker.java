package io.webby.perf;

import com.google.common.flogger.FluentLogger;
import io.webby.orm.api.TableObj;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;

public abstract class TableWorker<K, E> implements Runnable {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    protected final AtomicLong counter = new AtomicLong();
    protected final long maxCounter;
    protected final TableObj<K, E> table;
    protected final Supplier<K> keyGenerator;

    public TableWorker(@NotNull Init<K, E> init) {
        this.maxCounter = init.maxCounter;
        this.table = init.table;
        this.keyGenerator = init.keyGenerator;
    }

    @Override
    public void run() {
        try {
            while (counter.incrementAndGet() < maxCounter) {
                K key = keyGenerator.get();
                execute(key);
            }
        } catch (Throwable throwable) {
            log.at(Level.SEVERE).withCause(throwable).log("TableWorked crashed");
        }
    }

    public abstract void execute(@NotNull K key);

    public static <K, E> TableWorker<K, E> inserter(@NotNull Init<K, E> init, @NotNull Function<K, E> create) {
        return new TableWorker<>(init) {
            @Override
            public void execute(@NotNull K key) {
                if (table.getByPkOrNull(key) == null) {
                    log.at(Level.FINE).log("Inserting %s", key);
                    table.insert(create.apply(key));
                } else {
                    log.at(Level.FINE).log("Skipping %s", key);
                }
            }
        };
    }

    public static <K, E> TableWorker<K, E> updater(@NotNull Init<K, E> init, @NotNull Function<K, E> create) {
        return new TableWorker<>(init) {
            @Override
            public void execute(@NotNull K key) {
                log.at(Level.FINE).log("Updating %s", key);
                table.updateByPk(create.apply(key));
            }
        };
    }

    public static <K, E> TableWorker<K, E> deleter(@NotNull Init<K, E> init) {
        return new TableWorker<>(init) {
            @Override
            public void execute(@NotNull K key) {
                log.at(Level.FINE).log("Deleting %s", key);
                table.deleteByPk(key);
            }
        };
    }

    public static <K, E> TableWorker<K, E> reader(@NotNull Init<K, E> init) {
        return new TableWorker<>(init) {
            @Override
            public void execute(@NotNull K key) {
                log.at(Level.FINE).log("Reading %s", key);
                table.getByPkOrNull(key);
            }
        };
    }

    public record Init<K, E>(int maxCounter, @NotNull TableObj<K, E> table, @NotNull Supplier<K> keyGenerator) {
        public Init<K, E> withMaxCount(int newMaxCounter) {
            return new Init<>(newMaxCounter, table, keyGenerator);
        }
    }
}
