package io.webby.netty.intercept;

import com.google.common.collect.Lists;
import com.google.common.flogger.FluentLogger;
import com.google.common.flogger.util.CallerFinder;
import com.google.inject.Inject;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.webby.app.Settings;
import io.webby.netty.errors.ServeException;
import io.webby.netty.intercept.attr.AttributesValidator;
import io.webby.netty.marshal.Json;
import io.webby.netty.request.DefaultHttpRequestEx;
import io.webby.netty.response.HttpResponseFactory;
import io.webby.url.impl.Endpoint;
import io.webby.url.impl.EndpointContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class Interceptors {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    private final List<InterceptItem> stack;
    private final int attrBufferSize;
    private final boolean safeWrapperEnabled;
    private final Map<Integer, Interceptor> unsafeOwners;

    @Inject private HttpResponseFactory factory;
    @Inject private Json json;

    @Inject
    public Interceptors(@NotNull InterceptorScanner scanner, @NotNull Settings settings) {
        List<InterceptItem> items = scanner.getInterceptorsFromClasspath();
        int maxPosition = AttributesValidator.validateAttributeOwners(items).maxPosition();

        attrBufferSize = maxPosition + 1;
        stack = items.stream().filter(item -> item.instance().isEnabled()).toList();
        unsafeOwners = stack.stream()
                .filter(InterceptItem::isOwner)
                .filter(InterceptItem::canBeDisabled)
                .collect(Collectors.toMap(InterceptItem::position, InterceptItem::instance));
        safeWrapperEnabled = settings.isSafeMode() && !unsafeOwners.isEmpty();

        log.at(Level.FINE).log("Interceptors stack: %s", stack);
    }

    public @NotNull DefaultHttpRequestEx createRequest(@NotNull FullHttpRequest request,
                                                       @NotNull Channel channel,
                                                       @NotNull EndpointContext context) {
        Object[] attributes = new Object[attrBufferSize];  // empty, to be filled by interceptors
        DefaultHttpRequestEx requestEx = new DefaultHttpRequestEx(request, channel, json, context.constraints(), attributes);
        if (safeWrapperEnabled) {
            return new DefaultHttpRequestEx(requestEx) {
                @Override
                public <T> @NotNull T attrOrDie(int position) {
                    Interceptor owner = unsafeOwners.get(position);
                    if (owner != null) {
                        StackTraceElement caller = CallerFinder.findCallerOf(this.getClass(), new Throwable(), 0);
                        if (caller != null && !caller.getClassName().equals(owner.getClass().getName())) {
                            log.at(Level.WARNING).log("%s requested conditionally available attribute owned by %s", caller, owner);
                        }
                    }
                    return super.attrOrDie(position);
                }
            };
        }
        return requestEx;
    }

    public @Nullable HttpResponse enter(@NotNull DefaultHttpRequestEx request, @NotNull Endpoint endpoint) {
        for (InterceptItem item : stack) {
            Interceptor instance = item.instance();
            try {
                if (instance instanceof AdvancedInterceptor advanced) {
                    advanced.enter(request, endpoint);
                } else {
                    instance.enter(request);
                }
            } catch (ServeException e) {
                return factory.handleServeException(e, "Interceptor %s".formatted(instance));
            }
        }
        return null;
    }

    public @NotNull HttpResponse exit(@NotNull DefaultHttpRequestEx request, @NotNull HttpResponse response) {
        for (InterceptItem item : Lists.reverse(stack)) {
            response = item.instance().exit(request, response);
        }
        return response;
    }

    public void cleanup() {
        for (InterceptItem item : Lists.reverse(stack)) {
            item.instance().cleanup();
        }
    }

    public @NotNull Optional<Interceptor> findEnabledInterceptor(@NotNull Predicate<Interceptor> predicate) {
        return stack.stream().map(InterceptItem::instance).filter(predicate).findFirst();
    }
}
