package io.webby.url.ws;

import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface FrameMetaReader {
    void readMeta(@NotNull ByteBuf byteBuf, @NotNull Consumer consumer);

    interface Consumer {
        void accept(@Nullable ByteBuf acceptorId, long requestId);
    }
}
