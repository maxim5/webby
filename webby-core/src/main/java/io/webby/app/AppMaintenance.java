package io.webby.app;

import com.google.common.flogger.FluentLogger;
import com.google.inject.Inject;
import io.webby.netty.intercept.InterceptorsStack;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

public class AppMaintenance {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();
    private final Optional<AtomicBoolean> maintenanceMode;

    @Inject
    public AppMaintenance(@NotNull Settings settings, @NotNull InterceptorsStack interceptors) {
        maintenanceMode = interceptors
            .findEnabledInterceptor(interceptor -> interceptor instanceof AppMaintenanceInterceptor)
            .map(interceptor -> ((AppMaintenanceInterceptor) interceptor).maintenance());

        if (settings.isProdMode() && maintenanceMode.isEmpty()) {
            log.at(Level.WARNING).log("AppMaintenanceInterceptor is disabled: maintenance mode won't be available");
        }
    }

    public void trySetMaintenanceMode() {
        maintenanceMode.ifPresentOrElse(setter -> {
            if (setter.compareAndSet(false, true)) {
                log.at(Level.WARNING).log("Maintenance mode enabled: all incoming requests will receive 503");
            } else {
                log.at(Level.WARNING).log("Maintenance mode is already enabled");
            }
        }, () -> log.at(Level.WARNING).log("Maintenance mode is unavailable: AppMaintenanceInterceptor is disabled"));
    }
}
