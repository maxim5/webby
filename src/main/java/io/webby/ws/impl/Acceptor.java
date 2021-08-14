package io.webby.ws.impl;

import com.google.common.flogger.FluentLogger;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;

import static io.webby.util.Rethrow.rethrow;

public record Acceptor(@NotNull ByteBuf id, @NotNull String version, @NotNull Class<?> type, @NotNull Method method, boolean acceptsFrame) {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    public @Nullable Object call(@NotNull Object instance, @NotNull Object param) {
        try {
            Object callResult = method.invoke(instance, param);
            if (callResult == null && !isVoid(method)) {
                log.at(Level.WARNING).log("Websocket agent returned null: %s", method);
            }
            return callResult;
        } catch (IllegalAccessException | InvocationTargetException e) {
            return rethrow(e);
        }
    }

    private static boolean isVoid(@NotNull Method method) {
        return method.getReturnType().equals(Void.TYPE);
    }
}
