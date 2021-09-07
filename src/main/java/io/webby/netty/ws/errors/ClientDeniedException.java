package io.webby.netty.ws.errors;

import io.webby.netty.ws.Constants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ClientDeniedException extends WebsocketError {
    public ClientDeniedException(@NotNull String message, @Nullable Object @NotNull ... args) {
        super(Constants.StatusCodes.CLIENT_DENIED, message.formatted(args), null);
    }

    public ClientDeniedException(@NotNull String message, @Nullable String replyError) {
        super(Constants.StatusCodes.CLIENT_DENIED, message, replyError);
    }

    public ClientDeniedException(@NotNull String message, @Nullable Throwable cause, @Nullable String replyError) {
        super(Constants.StatusCodes.CLIENT_DENIED, message, cause, replyError);
    }

    public ClientDeniedException(@Nullable Throwable cause, @Nullable String replyError) {
        super(Constants.StatusCodes.CLIENT_DENIED, cause, replyError);
    }
}
