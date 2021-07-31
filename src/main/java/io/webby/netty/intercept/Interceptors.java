package io.webby.netty.intercept;

import com.google.common.collect.Lists;
import com.google.common.flogger.FluentLogger;
import com.google.common.flogger.util.CallerFinder;
import com.google.inject.Inject;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.webby.app.Settings;
import io.webby.netty.exceptions.*;
import io.webby.netty.intercept.attr.AttributesValidator;
import io.webby.netty.request.DefaultHttpRequestEx;
import io.webby.netty.response.HttpResponseFactory;
import io.webby.url.impl.Endpoint;
import io.webby.url.impl.EndpointContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class Interceptors {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    private final List<InterceptItem> stack;
    private final int attrBufferSize;
    private final boolean safeWrapperEnabled;
    private final Map<Integer, Interceptor> unsafeOwners;

    @Inject private HttpResponseFactory factory;

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
    }

    @NotNull
    public DefaultHttpRequestEx createRequest(@NotNull FullHttpRequest request, @NotNull EndpointContext context) {
        Object[] attributes = new Object[attrBufferSize];  // empty, to be filled by interceptors
        DefaultHttpRequestEx requestEx = new DefaultHttpRequestEx(request, context.constraints(), attributes);
        if (safeWrapperEnabled) {
            return new DefaultHttpRequestEx(requestEx) {
                @Override
                public @Nullable Object attr(int position) {
                    Interceptor owner = unsafeOwners.get(position);
                    if (owner != null) {
                        StackTraceElement caller = CallerFinder.findCallerOf(this.getClass(), new Throwable(), 0);
                        log.at(Level.WARNING).log("%s asks for %s", caller, owner);
                    }
                    return super.attr(position);
                }
            };
        }
        return requestEx;
    }

    @Nullable
    public FullHttpResponse enter(@NotNull DefaultHttpRequestEx request, @NotNull Endpoint endpoint) {
        for (InterceptItem item : stack) {
            Interceptor instance = item.instance();
            try {
                if (instance instanceof AdvancedInterceptor advanced) {
                    advanced.enter(request, endpoint);
                } else {
                    instance.enter(request);
                }
            } catch (BadRequestException e) {
                log.at(Level.INFO).withCause(e).log("Interceptor %s raised BAD_REQUEST: %s", instance, e.getMessage());
                return factory.newResponse400(e);
            } catch (UnauthorizedException e) {
                log.at(Level.INFO).withCause(e).log("Interceptor %s raised UNAUTHORIZED: %s", instance, e.getMessage());
                return factory.newResponse401(e);
            } catch (ForbiddenException e) {
                log.at(Level.INFO).withCause(e).log("Interceptor %s raised FORBIDDEN: %s", instance, e.getMessage());
                return factory.newResponse403(e);
            } catch (NotFoundException e) {
                log.at(Level.INFO).withCause(e).log("Interceptor %s raised NOT_FOUND: %s", instance, e.getMessage());
                return factory.newResponse404(e);
            } catch (RedirectException e) {
                log.at(Level.INFO).withCause(e).log("Interceptor %s raised REDIRECT: %s (%s): %s",
                        instance, e.uri(), e.isPermanent() ? "permanent" : "temporary", e.getMessage());
                return factory.newResponseRedirect(e.uri(), e.isPermanent());
            }
        }
        return null;
    }

    @NotNull
    public FullHttpResponse exit(@NotNull DefaultHttpRequestEx request, @NotNull FullHttpResponse response) {
        for (InterceptItem item : Lists.reverse(stack)) {
            response = item.instance().exit(request, response);
        }
        return response;
    }
}
