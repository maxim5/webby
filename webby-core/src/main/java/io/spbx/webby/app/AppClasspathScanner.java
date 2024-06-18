package io.spbx.webby.app;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.flogger.FluentLogger;
import com.google.inject.Inject;
import io.spbx.util.classpath.ClassNamePredicate;
import io.spbx.util.classpath.ClassPredicate;
import io.spbx.util.classpath.ClasspathScanner;
import io.spbx.util.classpath.GuavaClasspathScanner;
import io.spbx.util.lazy.AtomicLazyRecycle;
import io.spbx.util.lazy.LazyRecycle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.Set;
import java.util.function.Consumer;
import java.util.logging.Level;

public class AppClasspathScanner implements ClasspathScanner {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    // Currently, static to speed up tests. Guice injection is single-threaded.
    // Can consider dynamic class loading when necessary.
    private static final LazyRecycle<ClasspathScanner> impl = AtomicLazyRecycle.createUninitialized();

    @Inject
    public AppClasspathScanner(@NotNull EventBus eventBus) {
        eventBus.register(this);
    }

    @VisibleForTesting
    public AppClasspathScanner() {
    }

    @Subscribe
    public void guiceComplete(@NotNull GuiceCompleteEvent event) {
        log.at(Level.FINE).log("Optimizing AppClasspathScanner memory footprint: recycle classpath");
        impl.recycle();
    }

    @Override
    public void scan(@NotNull ClassNamePredicate classNamePredicate,
                     @NotNull ClassPredicate classPredicate,
                     @NotNull Consumer<Class<?>> consumer) {
        scanner().scan(classNamePredicate, classPredicate, consumer);
    }

    @Override
    public @NotNull Set<Class<?>> scanToSet(@NotNull ClassNamePredicate classNamePredicate,
                                            @NotNull ClassPredicate classPredicate) {
        return scanner().scanToSet(classNamePredicate, classPredicate);
    }

    private @NotNull ClasspathScanner scanner() {
        return impl.initializeIfNotYet(() -> GuavaClasspathScanner.fromClassLoader(ClassLoader.getSystemClassLoader()));
    }
}
