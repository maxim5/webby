package io.spbx.webby.ws.context;

import io.spbx.webby.netty.ws.FrameConst;

public record EmptyContext(boolean isTextRequest) implements BaseRequestContext {
    public static final BaseRequestContext EMPTY_TEXT_CONTEXT = new EmptyContext(true);
    public static final BaseRequestContext EMPTY_BINARY_CONTEXT = new EmptyContext(false);

    @Override
    public long requestId() {
        return FrameConst.RequestIds.NO_ID;
    }
}
