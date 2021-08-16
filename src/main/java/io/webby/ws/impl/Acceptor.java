package io.webby.ws.impl;

import com.google.common.flogger.FluentLogger;
import io.netty.buffer.ByteBuf;
import io.webby.url.impl.EndpointView;
import io.webby.url.view.Renderer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.logging.Level;

import static io.webby.util.Rethrow.rethrow;

public record Acceptor(@NotNull ByteBuf id, @NotNull String version,
                       @NotNull Class<?> type, @NotNull Method method,
                       @Nullable EndpointView<?> view, boolean acceptsFrame) {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    public @Nullable Object call(@NotNull Object instance, @NotNull Object param) {
        try {
            Object callResult = method.invoke(instance, param);
            if (callResult == null && !isVoid(method)) {
                log.at(Level.WARNING).log("Websocket agent returned null: %s", method);
            }
            if (view != null && callResult != null) {
                return render(view, callResult);
            }
            return callResult;
        } catch (Exception e) {
            return rethrow(e);
        }
    }

    private static <T> @NotNull Object render(@NotNull EndpointView<T> view, @NotNull Object callResult) throws Exception {
        Renderer<T> renderer = view.renderer();
        T template = view.template();
        return switch (renderer.support()) {
            case BYTE_ARRAY, BYTE_STREAM -> renderer.renderToBytes(template, callResult);
            case STRING -> renderer.renderToString(template, callResult);
        };
    }

    private static boolean isVoid(@NotNull Method method) {
        return method.getReturnType().equals(Void.TYPE);
    }
}
