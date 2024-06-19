package io.spbx.webby.url.caller;

import org.jetbrains.annotations.Nullable;

class CallOptions {
    protected final boolean wantsRequest;
    protected final ContentProvider contentProvider;

    public CallOptions(boolean wantsRequest, @Nullable ContentProvider contentProvider) {
        this.wantsRequest = wantsRequest;
        this.contentProvider = contentProvider;
    }

    boolean wantsContent() {
        return contentProvider != null;
    }

    RichCallOptions toRich(boolean swapArgs) {
        return new RichCallOptions(wantsRequest, contentProvider, swapArgs);
    }
}
