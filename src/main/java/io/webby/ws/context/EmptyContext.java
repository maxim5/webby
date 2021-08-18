package io.webby.ws.context;

import io.webby.netty.ws.Constants;

public record EmptyContext(boolean isTextRequest) implements BaseRequestContext {
    public static BaseRequestContext EMPTY_TEXT_CONTEXT = new EmptyContext(true);
    public static BaseRequestContext EMPTY_BINARY_CONTEXT = new EmptyContext(false);

    @Override
    public long requestId() {
        return Constants.RequestIds.NO_ID;
    }
}
