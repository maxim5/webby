package io.webby.netty.ws;

public class FrameConst {
    public static class StatusCodes {
        public static final int OK = 0;
        public static final int BAD_FRAME = 1;

        public static final int CLIENT_DENIED = 100;
    }

    public static class RequestIds {
        public static final long NO_ID = -1;
    }
}
