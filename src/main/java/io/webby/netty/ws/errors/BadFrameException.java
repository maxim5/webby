package io.webby.netty.ws.errors;

import io.webby.netty.ws.Constants;
import org.jetbrains.annotations.Nullable;

public class BadFrameException extends WebsocketError {
    public BadFrameException(String message) {
        super(Constants.StatusCodes.BAD_FRAME, message, null);
    }

    public BadFrameException(String message, @Nullable String replyError) {
        super(Constants.StatusCodes.BAD_FRAME, message, replyError);
    }

    public BadFrameException(String message, Throwable cause, @Nullable String replyError) {
        super(Constants.StatusCodes.BAD_FRAME, message, cause, replyError);
    }

    public BadFrameException(Throwable cause, @Nullable String replyError) {
        super(Constants.StatusCodes.BAD_FRAME, cause, replyError);
    }
}
