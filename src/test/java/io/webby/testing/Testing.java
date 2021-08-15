package io.webby.testing;

import com.google.gson.Gson;
import com.google.inject.Injector;
import com.google.inject.Module;
import io.webby.Webby;
import io.webby.app.AppSettings;
import io.webby.db.kv.StorageType;
import io.webby.netty.marshal.JsonMarshaller;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.config.Configurator;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

public class Testing {
    public static final boolean LOG_VERBOSE = false;

    public static final String DEFAULT_WEB_PATH = "src/test/resources";
    public static final String DEFAULT_VIEW_PATH = "src/test/resources";

    public static @NotNull Injector testStartupNoHandlers(@NotNull Module... modules) {
        return testStartup(settings -> settings.setInterceptorFilter((pkg, cls) -> false), modules);
    }

    public static @NotNull Injector testStartup(@NotNull Class<?> klass, @NotNull Module... modules) {
        return testStartup(settings -> settings.setHandlerClassOnly(klass), modules);
    }

    public static @NotNull Injector testStartup(@NotNull String packageName, @NotNull Module... modules) {
        return testStartup(settings -> settings.setHandlerPackageOnly(packageName), modules);
    }

    public static @NotNull Injector testStartup(@NotNull Consumer<AppSettings> consumer, @NotNull Module... modules) {
        AppSettings settings = new AppSettings();
        settings.setDevMode(true);
        settings.setSecurityKey("12345678901234567890123456789012");
        settings.setWebPath(DEFAULT_WEB_PATH);
        settings.setViewPath(DEFAULT_VIEW_PATH);
        settings.setHandlerFilter((pkg, cls) -> false);
        settings.setStorageType(StorageType.JAVA_MAP);
        consumer.accept(settings);
        return testStartup(settings, modules);
    }

    public static @NotNull Injector testStartup(@NotNull AppSettings settings, @NotNull Module... modules) {
        Configurator.setAllLevels(LogManager.ROOT_LOGGER_NAME, LOG_VERBOSE ? Level.TRACE : Level.WARN);

        Locale.setDefault(Locale.US);  // any way to remove this?

        return Webby.initGuice(settings, modules);
    }

    public static <K, V> @NotNull Map<K, V> asMap(Object ... items) {
        return asMap(List.of(items));
    }

    @SuppressWarnings("unchecked")
    public static <K, V> @NotNull Map<K, V> asMap(@NotNull List<?> items) {
        Assertions.assertEquals(0, items.size() % 2);
        LinkedHashMap<K, V> result = new LinkedHashMap<>();
        for (int i = 0; i < items.size(); i += 2) {
            result.put((K) items.get(i), (V) items.get(i + 1));
        }
        return result;
    }

    public static class Internals {
        public static final JsonMarshaller json = new JsonMarshaller() {
            {
                gson = new Gson();
            }
        };
    }
}
