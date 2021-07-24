package io.webby.url.view;

import com.google.common.flogger.FluentLogger;
import com.google.inject.*;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;
import java.util.logging.Level;

public class InjectorHelper {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    @Inject private Injector injector;

    @NotNull
    public <T> T getOrDefault(@NotNull Class<T> klass, @NotNull Supplier<T> defaultSupplier) {
        return getOrDefault(injector, klass, defaultSupplier);
    }

    @NotNull
    public static <T> T getOrDefault(@NotNull Injector injector,
                                     @NotNull Class<T> klass,
                                     @NotNull Supplier<T> defaultSupplier) {
        try {
            if (injector.findBindingsByType(TypeLiteral.get(klass)).size() > 0) {
                Provider<T> provider = injector.getProvider(klass);
                log.at(Level.FINE).log("Found explicit %s Guice provider: %s", klass, provider);
                return provider.get();
            }
        } catch (ConfigurationException e) {
            log.at(Level.FINEST).withCause(e).log("Failed to find provider for %s", klass);
        }

        log.at(Level.FINE).log("Applying default %s supplier", klass);
        return defaultSupplier.get();
    }
}
