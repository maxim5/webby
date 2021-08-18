package io.webby.netty.ws.errors;

import org.jetbrains.annotations.Nullable;

public abstract class WebsocketError extends RuntimeException {
    private final int code;
    private final @Nullable String replyError;

    public WebsocketError(int code, String message, @Nullable String replyError) {
        super(message);
        this.code = code;
        this.replyError = replyError;
    }

    public WebsocketError(int code, String message, Throwable cause, @Nullable String replyError) {
        super(message, cause);
        this.code = code;
        this.replyError = replyError;
    }

    public WebsocketError(int code, Throwable cause, @Nullable String replyError) {
        super(cause);
        this.code = code;
        this.replyError = replyError;
    }

    public int getCode() {
        return code;
    }

    public @Nullable String getReplyError() {
        return replyError;
    }
}
