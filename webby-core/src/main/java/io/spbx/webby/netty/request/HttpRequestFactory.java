package io.spbx.webby.netty.request;

import com.google.common.flogger.FluentLogger;
import com.google.common.flogger.util.CallerFinder;
import com.google.inject.Inject;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.spbx.webby.app.Settings;
import io.spbx.webby.netty.intercept.InterceptItem;
import io.spbx.webby.netty.intercept.Interceptor;
import io.spbx.webby.netty.intercept.InterceptorsStack;
import io.spbx.webby.netty.marshal.Json;
import io.spbx.webby.url.impl.EndpointContext;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class HttpRequestFactory {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    private final int attrBufferSize;
    private final boolean safeRequestWrapperEnabled;
    private final Map<Integer, Interceptor> unsafeOwners;

    @Inject private Json json;

    @Inject
    public HttpRequestFactory(@NotNull InterceptorsStack interceptors, @NotNull Settings settings) {
        attrBufferSize = interceptors.stack().stream()
            .filter(InterceptItem::isOwner)
            .mapToInt(InterceptItem::position)
            .max().orElse(-1) + 1;  // >= 0
        unsafeOwners = interceptors.stack().stream()
            .filter(InterceptItem::isOwner)
            .filter(InterceptItem::canBeDisabled)
            .collect(Collectors.toMap(InterceptItem::position, InterceptItem::instance));
        safeRequestWrapperEnabled = settings.isSafeMode() && !unsafeOwners.isEmpty();
    }

    public @NotNull DefaultHttpRequestEx createRequest(@NotNull FullHttpRequest request,
                                                       @NotNull Channel channel,
                                                       @NotNull EndpointContext context) {
        Object[] attributes = new Object[attrBufferSize];  // empty, to be filled by interceptors
        if (safeRequestWrapperEnabled) {
            return new DefaultHttpRequestEx(request, channel, json, context.constraints(), attributes) {
                @Override
                public <T> @NotNull T attrOrDie(int position) {
                    warnAboutUnsafeCall(position);
                    return super.attrOrDie(position);
                }

                private void warnAboutUnsafeCall(int position) {
                    Interceptor owner = unsafeOwners.get(position);
                    if (owner != null) {
                        StackTraceElement caller = CallerFinder.findCallerOf(this.getClass(), 0);
                        if (caller == null || !caller.getClassName().equals(owner.getClass().getName())) {
                            String message = "%s requested conditionally available attribute [%d] owned by %s. " +
                                "This call may fail in the future. Use #attr() method instead";
                            log.at(Level.WARNING).log(message, caller, position, owner);
                        }
                    }
                }
            };
        }
        return new DefaultHttpRequestEx(request, channel, json, context.constraints(), attributes);
    }
}
