package io.webby.testing;

import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;
import io.webby.Webby;
import io.webby.app.AppLifetime;
import io.webby.app.AppSettings;
import io.webby.auth.session.DefaultSession;
import io.webby.auth.user.DefaultUser;
import io.webby.app.AppClasspathScanner;
import io.webby.db.model.BlobKv;
import io.webby.netty.marshal.Json;
import io.webby.netty.marshal.Marshaller;
import io.webby.util.collect.Pair;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.config.Configurator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Testing {
    public static final String DEFAULT_WEB_PATH = ".";
    public static final String DEFAULT_VIEW_PATH = ".";
    public static final String DEFAULT_USER_CONTENT_PATH = ".";

    public static final List<Class<?>> AUTH_MODELS = List.of(DefaultSession.class, DefaultUser.class);
    public static final List<Class<?>> CORE_MODELS = List.of(DefaultSession.class, DefaultUser.class, BlobKv.class);

    public static @NotNull AppSettings defaultAppSettings() {
        AppSettings settings = new AppSettings();
        settings.setDevMode(true);
        settings.setCharset(TestingBytes.CHARSET);
        settings.setSecurityKey("12345678901234567890123456789012");
        settings.setWebPath(DEFAULT_WEB_PATH);
        settings.setViewPath(DEFAULT_VIEW_PATH);
        settings.setUserContentPath(DEFAULT_USER_CONTENT_PATH);
        settings.modelFilter().setPredicateUnsafe((pkg, cls) -> false);
        settings.handlerFilter().setPredicateUnsafe((pkg, cls) -> false);
        settings.interceptorFilter().setPredicateUnsafe((pkg, cls) -> false);
        settings.storageSettings()
            .disableKeyValue()
            .disableSql();
        return settings;
    }

    public static @NotNull Injector testStartup() {
        return testStartup(defaultAppSettings());
    }

    public static @NotNull Injector testStartup(@NotNull Module module) {
        return testStartup(defaultAppSettings(), module);
    }

    public static @NotNull Injector testStartup(@NotNull Consumer<AppSettings> consumer) {
        return testStartup(consumer, new Module[0]);
    }

    public static @NotNull Injector testStartup(@NotNull Consumer<AppSettings> consumer, @NotNull Module... modules) {
        AppSettings settings = defaultAppSettings();
        consumer.accept(settings);
        return testStartup(settings, modules);
    }

    public static @NotNull Injector testStartup(@NotNull AppSettings settings, @NotNull Module... modules) {
        return testStartup(settings, () -> {}, modules);
    }

    public static @NotNull Injector testStartup(@NotNull AppSettings settings,
                                                @NotNull Runnable callback,
                                                @NotNull Module... modules) {
        setLogLevelsFromSettings(settings);
        Locale.setDefault(Locale.US);  // any way to remove this?

        Module testingClasspathScanner = TestingModules.instance(new AppClasspathScanner());
        Module module = Modules.override(testingClasspathScanner).with(modules);
        Internals.injector = Webby.getReady(settings, module);
        callback.run();
        return Internals.injector;
    }

    private static void setLogLevelsFromSettings(@NotNull AppSettings settings) {
        String logging = settings.getProperty("testing.logging");
        if (logging == null) {
            Configurator.setAllLevels(LogManager.ROOT_LOGGER_NAME, Level.WARN);
        } else if (logging.contains("=")) {
            Configurator.setAllLevels(LogManager.ROOT_LOGGER_NAME, Level.WARN);
            String[] rules = logging.split(",");
            for (String rule : rules) {
                Pair<String, String> pair = Pair.of(rule.split("=", 2));
                Configurator.setLevel(pair.first(), Level.toLevel(pair.second()));
            }
        } else {
            Configurator.setAllLevels(LogManager.ROOT_LOGGER_NAME, Level.toLevel(logging));
        }
    }

    public static class Internals {
        private static Injector injector;

        public static @NotNull Injector injector() {
            assertNotNull(injector, "Test Guice Injector is not initialized");
            return injector;
        }

        public static <T> @NotNull T getInstance(@NotNull Class<T> type) {
            assertNotNull(injector, "Test Guice Injector is not initialized");
            return injector.getInstance(type);
        }

        public static <T> @Nullable T getInstanceOrNull(@NotNull Class<T> type) {
            return injector != null ? getInstance(type) : null;
        }

        public static @NotNull Charset charset() {
            return injector != null ? getInstance(Charset.class) : TestingBytes.CHARSET;
        }

        public static @NotNull AppSettings settings() {
            return getInstance(AppSettings.class);
        }

        public static @NotNull Json json() {
            // An alternative that always works:
            // new GsonMarshaller(new Gson(), TestingBytes.CHARSET)
            return injector != null ? getInstance(Json.class) : new LazyInternalJson();
        }

        public static void terminate() {
            if (injector != null) {
                injector.getInstance(AppLifetime.class).getLifetime().terminate();
            }
        }
    }

    private static class LazyInternalJson implements Json {
        @Override
        public @NotNull Charset charset() {
            assertNotNull(Internals.injector, "Test Guice Injector is not initialized");
            return Internals.charset();
        }

        @Override
        public @NotNull Marshaller withCustomCharset(@NotNull Charset charset) {
            assertNotNull(Internals.injector, "Test Guice Injector is not initialized");
            return Internals.json().withCustomCharset(charset);
        }

        @Override
        public void writeBytes(@NotNull OutputStream output, @NotNull Object instance) throws IOException {
            assertNotNull(Internals.injector, "Test Guice Injector is not initialized");
            Internals.json().writeBytes(output, instance);
        }

        @Override
        public <T> @NotNull T readBytes(@NotNull InputStream input, @NotNull Class<T> klass) throws IOException {
            assertNotNull(Internals.injector, "Test Guice Injector is not initialized");
            return Internals.json().readBytes(input, klass);
        }

        @Override
        public void writeChars(@NotNull Writer writer, @NotNull Object instance) throws IOException {
            assertNotNull(Internals.injector, "Test Guice Injector is not initialized");
            Internals.json().writeChars(writer, instance);
        }

        @Override
        public <T> @NotNull T readChars(@NotNull Reader reader, @NotNull Class<T> klass) throws IOException {
            assertNotNull(Internals.injector, "Test Guice Injector is not initialized");
            return Internals.json().readChars(reader, klass);
        }
    }
}
