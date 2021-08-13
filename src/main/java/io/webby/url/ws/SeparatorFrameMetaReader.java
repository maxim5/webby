package io.webby.url.ws;

import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

public record SeparatorFrameMetaReader(byte separator, int maxAcceptorIdSize) implements FrameMetaReader {
    @Override
    public void readMeta(@NotNull ByteBuf byteBuf, @NotNull Consumer consumer) {
        int index = byteBuf.indexOf(0, maxAcceptorIdSize, separator);
        if (index >= 0 && index + 10 <= byteBuf.readableBytes()) {
            ByteBuf id = byteBuf.readBytes(index);
            byteBuf.readBytes(1);  // separator
            long requestId = byteBuf.readLong();
            byteBuf.readBytes(1);  // separator
            consumer.accept(id, requestId);
        } else {
            consumer.accept(null, -1);
        }
    }
}
