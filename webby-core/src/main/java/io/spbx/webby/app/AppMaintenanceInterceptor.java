package io.spbx.webby.app;

import io.spbx.webby.netty.errors.ServeException;
import io.spbx.webby.netty.errors.ServiceUnavailableException;
import io.spbx.webby.netty.intercept.Interceptor;
import io.spbx.webby.netty.request.MutableHttpRequestEx;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicBoolean;

public class AppMaintenanceInterceptor implements Interceptor {
    private final AtomicBoolean maintenance = new AtomicBoolean(false);

    public @NotNull AtomicBoolean maintenance() {
        return maintenance;
    }

    @Override
    public void enter(@NotNull MutableHttpRequestEx request) throws ServeException {
        if (maintenance.get()) {
            throw new ServiceUnavailableException("Maintenance mode enabled");
        }
    }
}
