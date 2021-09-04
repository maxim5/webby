package io.webby;

import com.google.common.collect.Iterables;
import com.google.common.flogger.FluentLogger;
import com.google.common.flogger.util.CallerFinder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;
import com.google.inject.util.Modules;
import io.webby.app.AppConfigException;
import io.webby.app.AppModule;
import io.webby.app.AppSettings;
import io.webby.auth.AuthModule;
import io.webby.common.CommonModule;
import io.webby.db.DbModule;
import io.webby.db.kv.StorageType;
import io.webby.netty.NettyBootstrap;
import io.webby.netty.NettyModule;
import io.webby.perf.PerfModule;
import io.webby.url.UrlModule;
import io.webby.ws.WebsocketModule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Level;

public class Webby {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    public static @NotNull NettyBootstrap nettyBootstrap(@NotNull AppSettings settings, @NotNull Module ... modules) {
        Injector injector = initGuice(settings, modules);
        return injector.getInstance(NettyBootstrap.class);
    }

    public static @NotNull Injector initGuice(@NotNull AppSettings settings, @NotNull Module ... modules) {
        log.at(Level.INFO).log("Initializing Webby Guice module");
        Stage stage = (settings.isDevMode()) ? Stage.DEVELOPMENT : Stage.PRODUCTION;
        Iterable<Module> iterable = Iterables.concat(List.of(mainModule(settings)), List.of(modules));
        return Guice.createInjector(stage, iterable);
    }

    public static @NotNull Module mainModule(@NotNull AppSettings settings) throws AppConfigException {
        validateSettings(settings);
        return Modules.combine(
            new AppModule(settings),
            new AuthModule(),
            new CommonModule(),
            new DbModule(),
            new NettyModule(),
            new PerfModule(),
            new UrlModule(),
            new WebsocketModule()
        );
    }

    private static void validateSettings(@NotNull AppSettings settings) {
        validateSecurityKey(settings.securityKey());
        validateWebPath(settings.webPath());
        validateViewPaths(settings.viewPaths());
        validateHotReload(settings);
        validateProfileMode(settings);
        validateSafeMode(settings);
        validateStorageType(settings);
        validateHandlerFilter(settings);
        validateInterceptorFilter(settings);
    }

    private static void validateSecurityKey(byte @NotNull [] securityKey) {
        if (securityKey.length == 0) {
            throw new AppConfigException("Invalid settings: security key is not set. " +
                    "Please generate the secure random 32-byte string and use it in the app settings");
        }
        if (securityKey.length != 32) {
            throw new AppConfigException("Invalid settings: security key length must be 32 bytes");
        }
    }

    private static void validateWebPath(@Nullable Path webPath) {
        if (webPath == null) {
            throw new AppConfigException("Invalid settings: static web path is not set");
        }
        if (!Files.exists(webPath)) {
            throw new AppConfigException("Invalid settings: static web path does not exist: %s".formatted(webPath));
        }
    }

    private static void validateViewPaths(@Nullable List<Path> viewPaths) {
        if (viewPaths == null || viewPaths.isEmpty()) {
            throw new AppConfigException("Invalid settings: view paths are not set");
        }
        viewPaths.forEach(viewPath -> {
            if (viewPath == null) {
                throw new AppConfigException("Invalid settings: view path is null");
            }
            if (!Files.exists(viewPath)) {
                throw new AppConfigException("Invalid settings: view path does not exist: %s".formatted(viewPath));
            }
        });
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

    private static void validateStorageType(@NotNull AppSettings settings) {
        if (settings.storageType() == StorageType.JAVA_MAP && settings.isProdMode()) {
            log.at(Level.WARNING).log("Configured non-persistent storage in production: %s", settings.storageType());
        }
    }

    private static void validateHandlerFilter(@NotNull AppSettings settings) {
        if (!settings.isHandlerFilterSet()) {
            String className = getCallerClassName();
            if (className != null) {
                log.at(Level.FINER).log("Caller class name: %s", className);
                int dot = className.lastIndexOf('.');
                if (dot > 0) {
                    String packageName = className.substring(0, dot);
                    settings.setHandlerPackageOnly(packageName);
                    log.at(Level.INFO).log("Using package `%s` for classpath scanning (handlers)", packageName);
                    return;
                }
            }

            settings.setHandlerFilter((pkg, cls) -> true);
            log.at(Level.WARNING).log("Failed to determine enclosing package for classpath scanning. " +
                    "Using the whole classpath (can cause a delay and errors in loading classes)");
        }
    }

    private static void validateInterceptorFilter(@NotNull AppSettings settings) {
        if (!settings.isInterceptorFilterSet()) {
            String className = getCallerClassName();
            if (className != null) {
                log.at(Level.FINER).log("Caller class name: %s", className);
                int dot = className.lastIndexOf('.');
                if (dot > 0) {
                    String packageName = className.substring(0, dot);
                    settings.setInterceptorPackageOnly(packageName);
                    log.at(Level.INFO).log("Using package `%s` for classpath scanning (interceptors)", packageName);
                    return;
                }
            }

            settings.setInterceptorFilter((pkg, cls) -> true);
            log.at(Level.WARNING).log("Failed to determine enclosing package for classpath scanning. " +
                    "Using the whole classpath (can cause a delay and errors in loading classes)");
        }
    }

    @Nullable
    private static String getCallerClassName() {
        // `mainModule` is the earliest possible external call. The trace counts right now:
        //  - getCallerClassName     0
        //  - validateHandlerFilter  1
        //  - validateSettings       2
        //  - mainModule             3
        StackTraceElement caller = CallerFinder.findCallerOf(Webby.class, new Throwable(), 3);
        return caller != null ? caller.getClassName() : null;
    }
}
