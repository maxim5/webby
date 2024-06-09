package io.webby.common;

import com.google.common.flogger.FluentLogger;
import com.google.inject.*;
import com.google.inject.spi.BindingScopingVisitor;
import com.google.inject.spi.BindingTargetVisitor;
import com.google.inject.spi.ElementVisitor;
import com.google.inject.util.Providers;
import io.webby.app.Settings;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;

import static io.webby.util.base.EasyCast.castAny;

public class InjectorHelper {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    @Inject private Injector injector;
    @Inject private Settings settings;
    private final Map<Key<?>, Binding<?>> singletons = new ConcurrentHashMap<>();

    public <T> @NotNull T getOrDefault(@NotNull Class<? extends T> klass, @NotNull Supplier<? extends T> defaultSupplier) {
        return getOrDefault(injector, klass, defaultSupplier);
    }

    public <T> @NotNull T getOrDefault(@NotNull Class<? extends T> klass, @NotNull Function<Settings, ? extends T> defaultSupplier) {
        return getOrDefault(injector, klass, () -> defaultSupplier.apply(settings));
    }

    public <T> @NotNull T getOrDefault(@NotNull TypeLiteral<? extends T> typeLiteral, @NotNull Supplier<? extends T> defaultSupplier) {
        return getOrDefault(injector, typeLiteral, defaultSupplier);
    }

    public <T> @NotNull T getOrDefault(@NotNull TypeLiteral<? extends T> typeLiteral, @NotNull Function<Settings, ? extends T> defaultSupplier) {
        return getOrDefault(injector, typeLiteral, () -> defaultSupplier.apply(settings));
    }

    public static <T> @NotNull T getOrDefault(@NotNull Injector injector,
                                              @NotNull Class<? extends T> klass,
                                              @NotNull Supplier<? extends T> defaultSupplier) {
        return getOrDefault(injector, TypeLiteral.get(klass), defaultSupplier);
    }

    public static <T> @NotNull T getOrDefault(@NotNull Injector injector,
                                              @NotNull TypeLiteral<? extends T> typeLiteral,
                                              @NotNull Supplier<? extends T> defaultSupplier) {
        try {
            if (!injector.findBindingsByType(typeLiteral).isEmpty()) {  // perf: getBindings?
                Provider<? extends T> provider = injector.getProvider(Key.get(typeLiteral));
                log.at(Level.FINE).log("Found explicit %s Guice provider: %s", typeLiteral, provider);
                return provider.get();
            }
        } catch (ConfigurationException e) {
            log.at(Level.FINEST).withCause(e).log("Failed to find provider for %s", typeLiteral);
        }

        log.at(Level.FINE).log("Applying default %s supplier", typeLiteral);
        return defaultSupplier.get();
    }

    public <T> @NotNull T injectMembers(@NotNull T instance) {
        injector.injectMembers(instance);
        return instance;
    }

    // Replacement for injector.getInstance
    // See https://github.com/google/guice/issues/357
    public <T> @NotNull Binding<T> lazySingleton(@NotNull Key<T> key) {
        Binding<?> existing = injector.getBindings().get(key);
        if (existing == null) {
            return castAny(singletons.computeIfAbsent(key, __ -> constantBinding(key, injector.getBinding(key))));
        }
        return castAny(existing);
    }

    private <T> @NotNull Provider<T> lazySingletonProvider(@NotNull Class<T> klass) {
        return lazySingleton(Key.get(klass)).getProvider();
    }

    public <T> @NotNull T lazySingleton(@NotNull Class<T> klass) {
        return lazySingletonProvider(klass).get();
    }

    // Replacement for injector.getInstance
    public <T> @NotNull Binding<T> ensureSingleton(@NotNull Key<T> key) {
        return castAny(singletons.computeIfAbsent(key, __ -> constantBinding(key, injector.getBinding(key))));
    }

    private <T> @NotNull Provider<T> ensureSingletonProvider(@NotNull Class<T> klass) {
        return ensureSingleton(Key.get(klass)).getProvider();
    }

    public <T> @NotNull T ensureSingleton(@NotNull Class<T> klass) {
        return ensureSingletonProvider(klass).get();
    }

    public static <T, P> Provider<T> asProvider(@NotNull Class<P> klass, @NotNull Function<P, T> getter) {
        return new Provider<>() {
            @Inject private Injector injector;

            @Override
            public T get() {
                P instance = injector.getInstance(klass);
                return getter.apply(instance);
            }
        };
    }

    private static @NotNull <T> Binding<T> constantBinding(@NotNull Key<T> key, @NotNull Binding<T> binding) {
        T instance = binding.getProvider().get();
        return new Binding<>() {
            @Override
            public Key<T> getKey() {
                return key;
            }

            @Override
            public Provider<T> getProvider() {
                return Providers.of(instance);
            }

            @Override
            public <V> V acceptTargetVisitor(BindingTargetVisitor<? super T, V> visitor) {
                return binding.acceptTargetVisitor(visitor);
            }

            @Override
            public <V> V acceptScopingVisitor(BindingScopingVisitor<V> visitor) {
                return binding.acceptScopingVisitor(visitor);
            }

            @Override
            public Object getSource() {
                return binding.getSource();
            }

            @Override
            public <TT> TT acceptVisitor(ElementVisitor<TT> visitor) {
                return binding.acceptVisitor(visitor);
            }

            @Override
            public void applyTo(Binder binder) {
                binding.applyTo(binder);
            }
        };
    }
}
