package io.webby.perf;

import com.google.common.flogger.FluentLogger;
import io.webby.orm.api.TableObj;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;

public abstract class TableWorker<K, E> implements Worker {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    protected final long steps;
    protected final ProgressMonitor progress;
    protected final TableObj<K, E> table;
    protected final Supplier<K> keyGenerator;

    public TableWorker(@NotNull Init<K, E> init) {
        this.steps = init.steps;
        this.progress = init.progress;
        this.table = init.table;
        this.keyGenerator = init.keyGenerator;
    }

    @Override
    public void run() {
        progress.expectTotalSteps(steps);
        try {
            for (int i = 0; i < steps; i++) {
                progress.step();
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

    public static <K, E> TableWorker<K, E> scanner(@NotNull Init<K, E> init) {
        return new TableWorker<>(init) {
            @Override
            public void execute(@NotNull K key) {
                List<E> all = table.fetchAll();
                log.at(Level.FINE).log("Scanning over %d items", all.size());
            }
        };
    }

    public record Init<K, E>(long steps,
                             @NotNull ProgressMonitor progress,
                             @NotNull TableObj<K, E> table,
                             @NotNull Supplier<K> keyGenerator) {
        public Init {
            assert steps >= 0 : "Invalid steps: " + steps;
        }

        public Init<K, E> withSteps(long newSteps) {
            return new Init<>(newSteps, progress, table, keyGenerator);
        }

        public Init<K, E> withSteps(double stepsFactor) {
            return withSteps((long) (steps * stepsFactor));
        }
    }
}
