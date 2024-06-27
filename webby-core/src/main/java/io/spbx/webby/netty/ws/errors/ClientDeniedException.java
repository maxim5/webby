package io.spbx.webby.netty.ws.errors;

import io.spbx.webby.netty.ws.FrameConst;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ClientDeniedException extends WebsocketError {
    public ClientDeniedException(@NotNull String message, @Nullable Object @NotNull ... args) {
        super(FrameConst.StatusCodes.CLIENT_DENIED, message.formatted(args), null);
    }

    public ClientDeniedException(@NotNull String message, @Nullable String replyError) {
        super(FrameConst.StatusCodes.CLIENT_DENIED, message, replyError);
    }

    public ClientDeniedException(@NotNull String message, @Nullable Throwable cause, @Nullable String replyError) {
        super(FrameConst.StatusCodes.CLIENT_DENIED, message, cause, replyError);
    }

    public ClientDeniedException(@Nullable Throwable cause, @Nullable String replyError) {
        super(FrameConst.StatusCodes.CLIENT_DENIED, cause, replyError);
    }

    public static void assure(boolean cond, @NotNull String message, @Nullable Object @NotNull ... args) {
        if (!cond) {
            throw new ClientDeniedException(message, args);
        }
    }

    public static void failIf(boolean cond, @NotNull String message, @Nullable Object @NotNull ... args) {
        if (cond) {
            throw new ClientDeniedException(message, args);
        }
    }
}
