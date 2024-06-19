package io.spbx.webby.netty.ws.errors;

import io.spbx.webby.netty.ws.FrameConst;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BadFrameException extends WebsocketError {
    public BadFrameException(@NotNull String message, @Nullable Object @NotNull ... args) {
        super(FrameConst.StatusCodes.BAD_FRAME, message.formatted(args), null);
    }

    public BadFrameException(@NotNull String message, @Nullable String replyError) {
        super(FrameConst.StatusCodes.BAD_FRAME, message, replyError);
    }

    public BadFrameException(@NotNull String message, @Nullable Throwable cause, @Nullable String replyError) {
        super(FrameConst.StatusCodes.BAD_FRAME, message, cause, replyError);
    }

    public BadFrameException(@Nullable Throwable cause, @Nullable String replyError) {
        super(FrameConst.StatusCodes.BAD_FRAME, cause, replyError);
    }
}
