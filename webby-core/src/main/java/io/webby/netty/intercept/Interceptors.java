package io.webby.netty.intercept;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.flogger.FluentLogger;
import com.google.inject.Inject;
import io.netty.handler.codec.http.HttpResponse;
import io.webby.app.Settings;
import io.webby.netty.errors.ServeException;
import io.webby.netty.intercept.attr.AttributesValidator;
import io.webby.netty.marshal.Json;
import io.webby.netty.request.DefaultHttpRequestEx;
import io.webby.netty.response.HttpResponseFactory;
import io.webby.url.impl.Endpoint;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.List;
import java.util.function.Supplier;
import java.util.logging.Level;

public class Interceptors implements InterceptorsStack {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    private final ImmutableList<InterceptItem> stack;

    @Inject private HttpResponseFactory responses;
    @Inject private Json json;

    @Inject
    public Interceptors(@NotNull InterceptorScanner scanner, @NotNull Settings settings) {
        List<InterceptItem> items = scanner.getInterceptorsFromClasspath();
        AttributesValidator.validateAttributeOwners(items);
        stack = items.stream().filter(item -> item.instance().isEnabled()).collect(ImmutableList.toImmutableList());
        log.at(Level.FINE).log("Interceptors stack: %s", stack);
    }

    @Override
    public @NotNull ImmutableList<InterceptItem> stack() {
        return stack;
    }

    public @NotNull HttpResponse process(@NotNull DefaultHttpRequestEx requestEx,
                                         @NotNull Endpoint endpoint,
                                         @NotNull Supplier<HttpResponse> handleAction) {
        try {
            HttpResponse intercepted = enter(requestEx, endpoint);
            if (intercepted != null) {
                cleanup();
                return intercepted;
            }
            HttpResponse response = handleAction.get();
            return exit(requestEx, response);
        } catch (Throwable throwable) {
            cleanup();
            return responses.newResponse500("Unexpected failure", throwable);
        }
    }

    @VisibleForTesting
    @Nullable HttpResponse enter(@NotNull DefaultHttpRequestEx request, @NotNull Endpoint endpoint) {
        for (InterceptItem item : stack) {
            Interceptor instance = item.instance();
            try {
                if (instance instanceof AdvancedInterceptor advanced) {
                    advanced.enter(request, endpoint);
                } else {
                    instance.enter(request);
                }
            } catch (ServeException e) {
                return responses.handleServeException(e, "Interceptor %s".formatted(instance));
            }
        }
        return null;
    }

    @VisibleForTesting
    @NotNull HttpResponse exit(@NotNull DefaultHttpRequestEx request, @NotNull HttpResponse response) {
        for (InterceptItem item : Lists.reverse(stack)) {
            response = item.instance().exit(request, response);
        }
        return response;
    }

    @VisibleForTesting
    void cleanup() {
        for (InterceptItem item : Lists.reverse(stack)) {
            try {
                item.instance().cleanup();
            } catch (Throwable e) {
                log.at(Level.SEVERE).withCause(e).log("Interceptor clean-up failed for item=%s", item);
            }
        }
    }
}
