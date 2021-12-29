package io.webby;

import com.google.common.eventbus.EventBus;
import com.google.common.flogger.FluentLogger;
import com.google.common.flogger.util.CallerFinder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;
import com.google.inject.util.Modules;
import io.webby.app.*;
import io.webby.auth.AuthModule;
import io.webby.common.CommonModule;
import io.webby.common.GuiceCompleteEvent;
import io.webby.db.DbModule;
import io.webby.db.kv.KeyValueSettings;
import io.webby.db.kv.StorageType;
import io.webby.db.kv.impl.KeyValueStorageTypeDetector;
import io.webby.netty.NettyBootstrap;
import io.webby.netty.NettyModule;
import io.webby.orm.OrmModule;
import io.webby.perf.PerfModule;
import io.webby.url.UrlModule;
import io.webby.ws.WebsocketModule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;

import static io.webby.app.AppConfigException.assure;
import static io.webby.app.AppConfigException.failIf;

public class Webby {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    public static @NotNull NettyBootstrap nettyBootstrap(@NotNull AppSettings settings, @NotNull Module ... modules) {
        Injector injector = getReady(settings, modules);
        return injector.getInstance(NettyBootstrap.class);
    }

    public static @NotNull Injector getReady(@NotNull AppSettings settings, @NotNull Module ... modules) {
        return initGuice(settings, modules);
    }

    public static @NotNull Injector initGuice(@NotNull AppSettings settings, @NotNull Module ... modules) {
        log.at(Level.INFO).log("Initializing Webby Guice module");
        Stage stage = settings.isDevMode() ? Stage.DEVELOPMENT : Stage.PRODUCTION;
        Injector injector = Guice.createInjector(stage, Modules.override(mainModule(settings)).with(modules));
        injector.getInstance(EventBus.class).post(new GuiceCompleteEvent());
        return injector;
    }

    public static @NotNull Module mainModule(@NotNull AppSettings settings) throws AppConfigException {
        validateSettings(settings);
        return Modules.combine(
            new AppModule(settings),
            new AuthModule(settings),
            new CommonModule(),
            new DbModule(settings),
            new NettyModule(),
            new OrmModule(),
            new PerfModule(),
            new UrlModule(),
            new WebsocketModule()
        );
    }

    private static void validateSettings(@NotNull AppSettings settings) {
        validateSecurityKey(settings.securityKey());
        validateWebPath(settings.webPath());
        validateViewPaths(settings.viewPaths());
        validateUserContentPath(settings.userContentPath());
        validateHotReload(settings);
        validateProfileMode(settings);
        validateSafeMode(settings);
        validateStorageSettings(settings, settings.storageSettings());

        validateFilter(settings.modelFilter(), "models");
        validateFilter(settings.handlerFilter(), "handlers");
        validateFilter(settings.interceptorFilter(), "interceptors");
    }

    private static void validateStorageSettings(@NotNull Settings settings, @NotNull StorageSettings storageSettings) {
        new StorageValidator(settings, storageSettings).validate();
    }

    private record StorageValidator(@NotNull Settings settings, @NotNull StorageSettings storageSettings) {
        public void validate() {
            if (storageSettings.isKeyValueEnabled()) {
                validateKeyValues();
            }
        }

        private void validateKeyValues() {
            if (kvSettings() == KeyValueSettings.AUTO_DETECT) {
                failIf(settings.isProdMode(), "Auto-detect can't be used in production. Set the storage type explicitly");
                StorageType autoStorageType = KeyValueStorageTypeDetector.autoDetectStorageTypeFromClasspath();
                if (autoStorageType != null) {
                    updateKvType(autoStorageType);
                    log.at(Level.WARNING).log("Key-value storage auto-detect configured, " +
                                              "using type from classpath: %s", currentType());
                } else if (storageSettings.isSqlEnabled()) {
                    updateKvType(StorageType.SQL_DB);
                    log.at(Level.WARNING).log("Key-value storage auto-detect configured. Using %s", currentType());
                } else {
                    updateKvType(KeyValueSettings.DEFAULT_TYPE);
                    log.at(Level.WARNING).log("Key-value storage auto-detect configured, " +
                                              "using default: %s", currentType());
                }
            }

            if (currentType() == StorageType.JAVA_MAP && settings.isProdMode()) {
                log.at(Level.WARNING).log("Configured in-memory key-value storage in production: %s", currentType());
            }
            if (currentType() == StorageType.SQL_DB && settings.isProdMode()) {
                log.at(Level.WARNING).log("Configured inefficient key-value storage in production: %s", currentType());
            }
            if (currentType() != StorageType.JAVA_MAP && currentType() != StorageType.SQL_DB) {
                validateDirectory(kvSettings().path(), "storage path", settings.isDevMode());
            }

            if (currentType() == StorageType.SQL_DB && !storageSettings.isSqlEnabled()) {
                log.at(Level.WARNING).log("SQL disabled. Ignoring the configured type: %s. " +
                                          "Using default instead", currentType());
                updateKvType(KeyValueSettings.DEFAULT_TYPE);
            }
        }

        private @NotNull KeyValueSettings kvSettings() {
            return storageSettings.keyValueSettingsOrDie();
        }

        private @NotNull StorageType currentType() {
            return kvSettings().type();
        }

        private void updateKvType(@NotNull StorageType storageType) {
            if (storageSettings.isKeyValueEnabled()) {
                storageSettings.enableKeyValue(storageSettings.keyValueSettingsOrDie().with(storageType));
            }
        }
    }

    private static void validateSecurityKey(byte @NotNull [] securityKey) {
        // TODO: be pro-active and suggest settings
        assure(securityKey.length > 0, "Invalid settings: security key is not set. " +
                                       "Please generate the secure random 32-byte string and use it in the app settings");
        assure(securityKey.length == 32, "Invalid settings: security key length must be 32 bytes");
    }

    private static void validateWebPath(@Nullable Path webPath) {
        validateDirectory(webPath, "static web path", false);
    }

    private static void validateViewPaths(@Nullable List<Path> viewPaths) {
        failIf(isNullOrEmpty(viewPaths), "Invalid settings: view paths are not set");
        viewPaths.forEach(viewPath -> validateDirectory(viewPath, "view path", false));
    }

    private static void validateUserContentPath(@Nullable Path userContentPath) {
        validateDirectory(userContentPath, "user content path", false);  // TODO: true for dev?
    }

    private static void validateDirectory(@Nullable Path path, @NotNull String name, boolean autoCreate) {
        assure(path != null, "Invalid settings: %s is not set", name);
        File file = path.toFile();
        if (!file.exists()) {
            if (autoCreate) {
                log.at(Level.INFO).log("The %s does not exist. Creating directory: %s", name, path);
                assure(file.mkdirs(), "Failed to create %s directory: %s", name, path);
            } else {
                throw new AppConfigException("Invalid settings: %s does not exist: %s", name, path);
            }
        }
        assure(file.isDirectory(), "Invalid settings: %s must be a directory: %s", name, path);
    }

    private static void validateHotReload(@NotNull AppSettings settings) {
        if (settings.isHotReloadDefault()) {
            settings.setHotReload(settings.isDevMode());
        } else {
            if (settings.isHotReload() && settings.isProdMode()) {
                log.at(Level.WARNING).log("Configured hot reload in production");
            }
        }
    }

    private static void validateProfileMode(@NotNull AppSettings settings) {
        if (settings.isProfileModeDefault()) {
            settings.setProfileMode(settings.isDevMode());
        } else {
            if (settings.isProfileMode() && settings.isProdMode()) {
                log.at(Level.WARNING).log("Configured profile mode in production");
            }
        }
    }

    private static void validateSafeMode(@NotNull AppSettings settings) {
        if (settings.isSafeModeDefault()) {
            settings.setSafeMode(settings.isDevMode());
        } else {
            if (settings.isSafeMode() && settings.isProdMode()) {
                log.at(Level.WARNING).log("Configured safe mode in production");
            }
        }
    }

    private static void validateFilter(@NotNull ClassFilter classFilter, @NotNull String filterName) {
        if (!classFilter.isSet()) {
            String className = getCallerClassName();
            if (className != null) {
                log.at(Level.FINER).log("Caller class name: %s", className);
                int dot = className.lastIndexOf('.');
                if (dot > 0) {
                    String packageName = className.substring(0, dot);
                    classFilter.setPackageOnly(packageName);
                    log.at(Level.INFO).log("Using package `%s` for classpath scanning [%s]", packageName, filterName);
                    return;
                }
            }

            classFilter.setWholeClasspath();
            log.at(Level.WARNING).log(
                "Failed to determine enclosing package for classpath scanning [%s]. " +
                "Using the whole classpath. This may cause noticeable delays and errors in loading classes.",
                filterName
            );
        }
    }

    private static @Nullable String getCallerClassName() {
        // `mainModule` is the earliest possible external call. The trace counts right now:
        //  - getCallerClassName     0
        //  - validateFilter         1
        //  - validateSettings       2
        //  - mainModule             3
        StackTraceElement caller = CallerFinder.findCallerOf(Webby.class, 3);
        return caller != null ? caller.getClassName() : null;
    }

    private static boolean isNullOrEmpty(@Nullable Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }
}
