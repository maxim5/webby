package io.webby.benchmarks.stress;

import com.google.common.flogger.FluentLogger;
import io.webby.orm.api.TableObj;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;

public abstract class TableWorker<K, E> implements Worker {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    protected final long steps;
    protected final ProgressMonitor progress;
    protected final TableObj<K, E> table;
    protected final Supplier<K> keyGenerator;
    private final Consumer<Integer> listener;

    public TableWorker(@NotNull Init<K, E> init) {
        steps = init.steps;
        progress = init.progress;
        table = init.table;
        keyGenerator = init.keyGenerator;
        listener = init.listener;
    }

    @Override
    public void run() {
        progress.expectTotalSteps(steps);
        try {
            for (int i = 0; i < steps; i++) {
                progress.step();
                K key = keyGenerator.get();
                execute(key);
                listener.accept(i);
            }
        } catch (Throwable throwable) {
            log.at(Level.SEVERE).withCause(throwable).log("TableWorker crashed");
        }
    }

    protected abstract void execute(@NotNull K key);

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
                             @NotNull Supplier<K> keyGenerator,
                             @NotNull Consumer<Integer> listener) {
        public Init {
            assert steps >= 0 : "Invalid steps: " + steps;
        }

        public Init<K, E> withSteps(long newSteps) {
            return new Init<>(newSteps, progress, table, keyGenerator, listener);
        }

        public Init<K, E> withSteps(double stepsFactor) {
            return withSteps((long) (steps * stepsFactor));
        }
    }
}
