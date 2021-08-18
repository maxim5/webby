package io.webby.app;

import io.webby.netty.errors.ServeException;
import io.webby.netty.errors.ServiceUnavailableException;
import io.webby.netty.intercept.Interceptor;
import io.webby.netty.request.MutableHttpRequestEx;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicBoolean;

public class AppMaintenanceInterceptor implements Interceptor {
    private final AtomicBoolean maintenance = new AtomicBoolean(false);

    @NotNull
    public AtomicBoolean maintenance() {
        return maintenance;
    }

    @Override
    public void enter(@NotNull MutableHttpRequestEx request) throws ServeException {
        if (maintenance.get()) {
            throw new ServiceUnavailableException("Maintenance mode enabled");
        }
    }
}
