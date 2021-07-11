package io.webby.url.caller;

import io.webby.url.impl.ContentProvider;
import org.jetbrains.annotations.Nullable;

class RichCallOptions extends CallOptions {
    protected final boolean wantsBuffer1;
    protected final boolean wantsBuffer2;
    protected final boolean swapArgs;

    public RichCallOptions(boolean wantsRequest,
                           @Nullable ContentProvider contentProvider,
                           boolean wantsBuffer1,
                           boolean wantsBuffer2,
                           boolean swapArgs) {
        super(wantsRequest, contentProvider);
        this.wantsBuffer1 = wantsBuffer1;
        this.wantsBuffer2 = wantsBuffer2;
        this.swapArgs = swapArgs;
    }
}
