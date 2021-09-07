package io.webby.netty.ws.errors;

import io.webby.netty.ws.Constants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BadFrameException extends WebsocketError {
    public BadFrameException(@NotNull String message, @Nullable Object @NotNull ... args) {
        super(Constants.StatusCodes.BAD_FRAME, message.formatted(args), null);
    }

    public BadFrameException(@NotNull String message, @Nullable String replyError) {
        super(Constants.StatusCodes.BAD_FRAME, message, replyError);
    }

    public BadFrameException(@NotNull String message, @Nullable Throwable cause, @Nullable String replyError) {
        super(Constants.StatusCodes.BAD_FRAME, message, cause, replyError);
    }

    public BadFrameException(@Nullable Throwable cause, @Nullable String replyError) {
        super(Constants.StatusCodes.BAD_FRAME, cause, replyError);
    }
}
