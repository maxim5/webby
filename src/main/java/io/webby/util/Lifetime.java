package io.webby.util;

import com.google.common.flogger.FluentLogger;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.util.ArrayDeque;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;

public abstract class Lifetime {
    // See https://stackoverflow.com/questions/48755164/referencing-subclass-in-static-variable
    // and https://stackoverflow.com/questions/50021182/java-static-initializers-referring-to-subclasses-avoid-class-loading-deadlock
    @SuppressWarnings("StaticInitializerReferencesSubClass")
    public static final Lifetime Eternal = new Definition();

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
            nested.execute(() -> atomicAction.accept(nested));
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

        @Override
        public void onTerminate(@NotNull Closeable closeable) {
            tryToAdd(closeable);
        }

        @Override
        protected void attach(@NotNull Lifetime.Definition child) {
            if (child == Eternal) {
                throw new IllegalArgumentException("Eternal lifetime can't be attached");
            }
            if (!tryToAdd(child)) {
                log.at(Level.WARNING).log("Failed to attach a child lifetime: %s", child);
            }
        }

        public boolean terminate() {  // TODO: is terminating
            if (this == Eternal) {
                return false;
            }
            deconstruct();
            return true;
        }

        private boolean tryToAdd(@NotNull Object resource) {
            if (this == Eternal) {
                return true;
            }
            resources.add(resource);
            return true;
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
                        log.at(Level.SEVERE).log("Failed to terminal unexpected resource: %s", resource);
                    }
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                    log.at(Level.WARNING).withCause(throwable).log("Runnable failed: %s", throwable.getMessage());
                }
            }
        }

        public <E extends Throwable> void execute(@NotNull ThrowRunnable<E> runnable) throws E {
            runnable.run();
        }
    }
}
