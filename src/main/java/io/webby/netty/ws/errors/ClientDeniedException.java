package io.webby.netty.ws.errors;

import io.webby.netty.ws.Constants;
import org.jetbrains.annotations.Nullable;

public class ClientDeniedException extends WebsocketError {
    public ClientDeniedException(String message) {
        super(Constants.StatusCodes.CLIENT_DENIED, message, null);
    }

    public ClientDeniedException(String message, @Nullable String replyError) {
        super(Constants.StatusCodes.CLIENT_DENIED, message, replyError);
    }

    public ClientDeniedException(String message, Throwable cause, @Nullable String replyError) {
        super(Constants.StatusCodes.CLIENT_DENIED, message, cause, replyError);
    }

    public ClientDeniedException(Throwable cause, @Nullable String replyError) {
        super(Constants.StatusCodes.CLIENT_DENIED, cause, replyError);
    }
}
