package io.spbx.webby.ws.meta;

import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface FrameMetadata {
    int MAX_ID_SIZE = 64;

    void parse(@NotNull ByteBuf frameContent, @NotNull MetadataConsumer consumer);

    @NotNull ByteBuf compose(long requestId, int code, byte @NotNull [] content);

    interface MetadataConsumer {
        void accept(@Nullable ByteBuf acceptorId, long requestId, @NotNull ByteBuf content);
    }
}
