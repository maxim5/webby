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
import io.webby.common.CommonModule;
import io.webby.netty.NettyBootstrap;
import io.webby.netty.NettyModule;
import io.webby.url.UrlModule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Level;

public class Webby {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    @NotNull
    public static NettyBootstrap nettyBootstrap(@NotNull AppSettings settings, @NotNull Module ... modules) {
        Injector injector = initGuice(settings, modules);
        return injector.getInstance(NettyBootstrap.class);
    }

    @NotNull
    public static Injector initGuice(@NotNull AppSettings settings, @NotNull Module ... modules) {
        log.at(Level.INFO).log("Initializing Webby Guice module");
        Stage stage = (settings.isDevMode()) ? Stage.DEVELOPMENT : Stage.PRODUCTION;
        Iterable<Module> iterable = Iterables.concat(List.of(mainModule(settings)), List.of(modules));
        return Guice.createInjector(stage, iterable);
    }

    @NotNull
    public static Module mainModule(@NotNull AppSettings settings) throws AppConfigException {
        validateSettings(settings);
        return Modules.combine(new AppModule(settings), new CommonModule(), new NettyModule(), new UrlModule());
    }

    private static void validateSettings(@NotNull AppSettings settings) {
        validateWebPath(settings.webPath());
        validateViewPaths(settings.viewPaths());
        validateHotReload(settings);
        validatePackageTester(settings);
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
