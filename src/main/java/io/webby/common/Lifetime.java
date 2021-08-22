package io.webby.common;

import com.google.common.flogger.FluentLogger;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.webby.util.func.ThrowRunnable;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.util.ArrayDeque;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class Lifetime {
    // See https://stackoverflow.com/questions/48755164/referencing-subclass-in-static-variable
    // and https://stackoverflow.com/questions/50021182/java-static-initializers-referring-to-subclasses-avoid-class-loading-deadlock
    @SuppressWarnings("StaticInitializerReferencesSubClass")
    public static final Lifetime Eternal = new Definition();

    @NotNull
    public abstract Status status();

    @NotNull
    public Lifetime.Definition createNested() {
        Definition child = new Definition();
        attach(child);
        return child;
    }

    @NotNull
    public Lifetime.Definition createNested(@NotNull Consumer<Definition> atomicAction) {
        Definition nested = createNested();
        try {
            nested.executeIfAlive(() -> atomicAction.accept(nested));
            return nested;
        } catch (Throwable throwable) {
            nested.terminate();
            throw throwable;
        }
    }

    public <T> T usingNested(@NotNull Function<Lifetime, T> action) {
        Definition nested = createNested();
        try {
            return action.apply(nested);
        } finally {
            nested.terminate();
        }
    }

    public abstract void onTerminate(@NotNull Closeable closeable);

    protected abstract void attach(@NotNull Lifetime.Definition child);

    public static class Definition extends Lifetime {
        private static final FluentLogger log = FluentLogger.forEnclosingClass();

        private final ArrayDeque<Object> resources = new ArrayDeque<>();
        private final AtomicReference<Status> status = new AtomicReference<>(Status.Alive);

        @Override
        @NotNull
        public Status status() {
            return status.get();
        }

        @Override
        public void onTerminate(@NotNull Closeable closeable) {
            tryToAdd(closeable);
        }

        @Override
        protected void attach(@NotNull Lifetime.Definition child) {
            if (child == Eternal) {
                throw new IllegalArgumentException("Eternal lifetime can't be attached");
            }
            tryToAdd(child);
        }

        @CanIgnoreReturnValue
        public boolean terminate() {
            if (this == Eternal) {
                return false;
            }
            if (status.compareAndSet(Status.Alive, Status.Terminating)) {
                deconstruct();
                status.set(Status.Terminated);
                return true;
            } else {
                if (status.get() == Status.Terminating) {
                    log.atSevere().log("Lifetime is already terminating");
                }
                return false;
            }
        }

        public void terminateIfAlive() {
            if (status.get() == Status.Alive) {
                terminate();
            }
        }

        private void tryToAdd(@NotNull Object resource) {
            if (this == Eternal) {
                return;
            }
            if (status.get() == Status.Alive) {
                resources.addLast(resource);
            } else {
                log.atWarning().log("Failed to add a resource due to status=%s: %s", status.get(), resource);
            }
        }

        private void deconstruct() {
            while (!resources.isEmpty()) {
                Object resource = resources.pollFirst();
                try {
                    if (resource instanceof Closeable closeable) {
                        closeable.close();
                    } else if (resource instanceof ThrowRunnable<?> runnable) {
                        runnable.run();
                    } else if (resource instanceof Definition definition) {
                        definition.terminate();
                    } else {
                        log.atSevere().log("Failed to terminal unexpected resource: %s", resource);
                    }
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                    log.atWarning().withCause(throwable).log("Runnable failed: %s", throwable.getMessage());
                }
            }
        }

        public <E extends Throwable> void executeIfAlive(@NotNull ThrowRunnable<E> runnable) throws E {
            if (status.compareAndSet(Status.Alive, Status.Alive)) {
                runnable.run();
            }
        }
    }

    public enum Status {
        Alive,
        Terminating,
        Terminated
    }
}
