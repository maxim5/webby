package io.webby.url.caller;

import org.jetbrains.annotations.Nullable;

class RichCallOptions extends CallOptions {
    protected final boolean swapArgs;

    public RichCallOptions(boolean wantsRequest,
                           @Nullable ContentProvider contentProvider,
                           boolean swapArgs) {
        super(wantsRequest, contentProvider);
        this.swapArgs = swapArgs;
    }
}
