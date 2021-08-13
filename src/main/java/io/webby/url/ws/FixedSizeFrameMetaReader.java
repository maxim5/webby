package io.webby.url.ws;

import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

public record FixedSizeFrameMetaReader(int size) implements FrameMetaReader {
    @Override
    public void readMeta(@NotNull ByteBuf byteBuf, @NotNull Consumer consumer) {
        if (byteBuf.readableBytes() >= size + 8) {
            ByteBuf acceptorId = byteBuf.readBytes(size);
            long requestId = byteBuf.readLong();
            consumer.accept(acceptorId, requestId);
        } else {
            consumer.accept(null, -1);
        }
    }
}
