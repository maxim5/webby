package io.webby.url.caller;

import io.webby.url.impl.ContentProvider;
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

    RichCallOptions toRichStr(boolean wantsBuffer) {
        return new RichCallOptions(wantsRequest, contentProvider, wantsBuffer, wantsBuffer, false);
    }

    RichCallOptions toRichStrInt(boolean wantsBuffer, boolean swapArgs) {
        return new RichCallOptions(wantsRequest, contentProvider, wantsBuffer, wantsBuffer, swapArgs);
    }

    RichCallOptions toRichStrStr(boolean wantsBuffer1, boolean wantsBuffer2) {
        return new RichCallOptions(wantsRequest, contentProvider, wantsBuffer1, wantsBuffer2, false);
    }
}
