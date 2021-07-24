package io.webby;

import com.google.common.flogger.FluentLogger;
import com.google.common.flogger.util.CallerFinder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.webby.app.AppConfigException;
import io.webby.app.AppModule;
import io.webby.app.AppSettings;
import io.webby.netty.NettyModule;
import io.webby.url.UrlModule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import java.io.File;
import java.util.logging.Level;

public class Webby {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    @NotNull
    public static Injector initDependencies(@NotNull AppSettings settings) throws AppConfigException {
        log.at(Level.INFO).log("Initializing Webby dependencies");
        validateSettings(settings);
        return initGuice(settings);
    }

    @VisibleForTesting
    /*package*/ static Injector initGuice(@NotNull AppSettings settings) {
        return Guice.createInjector(new AppModule(settings), new NettyModule(), new UrlModule());
    }

    private static void validateSettings(@NotNull AppSettings settings) {
        validateWebPath(settings.webPath());
        validateViewPath(settings.viewPath());
        validateHotReload(settings);
        validatePackageTester(settings);
    }

    private static void validateWebPath(@Nullable String webPath) {
        if (webPath == null) {
            throw new AppConfigException("Invalid settings: static web path is not set");
        }
        if (!new File(webPath).exists()) {
            throw new AppConfigException("Invalid settings: static web path does not exist: %s".formatted(webPath));
        }
    }

    private static void validateViewPath(@Nullable String viewPath) {
        if (viewPath == null) {
            throw new AppConfigException("Invalid settings: view path is not set");
        }
        if (!new File(viewPath).exists()) {
            throw new AppConfigException("Invalid settings: view path does not exist: %s".formatted(viewPath));
        }
    }

    private static void validateHotReload(@NotNull AppSettings settings) {
        if (settings.isHotReloadDefault()) {
            settings.setHotReload(settings.isDevMode());
        } else {
            if (settings.isHotReload() && !settings.isDevMode()) {
                log.at(Level.WARNING).log("Configured hot reload in production mode");
            }
        }
    }

    private static void validatePackageTester(@NotNull AppSettings settings) {
        if (settings.filter() == null) {
            String className = getCallerClassName();
            if (className != null) {
                log.at(Level.FINER).log("Caller class name: %s", className);
                int dot = className.lastIndexOf('.');
                if (dot > 0) {
                    String packageName = className.substring(0, dot);
                    settings.setPackageOnly(packageName);
                    log.at(Level.INFO).log("Using package `%s` for classpath scanning", packageName);
                    return;
                }
            }

            settings.setFilter((pkg, cls) -> true);
            log.at(Level.WARNING).log("Failed to determine enclosing package for classpath scanning. " +
                    "Using the whole classpath (can cause a delay and errors in loading classes)");
        }
    }

    @Nullable
    private static String getCallerClassName() {
        StackTraceElement caller = CallerFinder.findCallerOf(Webby.class, new Throwable(), 3);
        return caller != null ? caller.getClassName() : null;
    }
}
