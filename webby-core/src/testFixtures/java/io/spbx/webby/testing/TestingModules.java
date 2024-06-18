package io.spbx.webby.testing;

import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.util.Modules;
import io.spbx.webby.testing.db.sql.TestingPersistentDbTableCleaner;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicReference;

import static io.spbx.util.base.EasyCast.castAny;

public class TestingModules {
    public static final Module PERSISTENT_DB_CLEANER_MODULE = singleton(TestingPersistentDbTableCleaner.class);

    public static <T> @NotNull Module instance(@NotNull Class<? super T> klass, @NotNull T instance) {
        return binder -> binder.bind(klass).toInstance(instance);
    }

    public static <T> @NotNull Module instance(@NotNull T instance) {
        return binder -> binder.bind(instance.getClass()).toInstance(castAny(instance));
    }

    public static <T> @NotNull Module mayBeInstance(@NotNull Class<? super T> klass, @Nullable T instance) {
        return instance != null ? instance(klass, instance) : Modules.EMPTY_MODULE;
    }

    public static <T> @NotNull Module mayBeInstance(@Nullable T instance) {
        return instance != null ? instance(instance) : Modules.EMPTY_MODULE;
    }

    public static <T> @NotNull Module provided(@NotNull Class<T> klass, @NotNull Provider<T> provider) {
        return binder -> binder.bind(klass).toProvider(provider);
    }

    public static <T> @NotNull Module provided(@NotNull Class<T> klass, @NotNull AtomicReference<T> reference) {
        return binder -> binder.bind(klass).toProvider(reference::get);
    }

    public static <T> @NotNull Module override(@NotNull Class<T> base, @NotNull Class<? extends T> derived) {
        return binder -> binder.bind(base).to(derived).asEagerSingleton();
    }

    public static <T> @NotNull Module singleton(@NotNull Class<? super T> klass) {
        return binder -> binder.bind(klass).asEagerSingleton();
    }
}
