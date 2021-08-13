package io.webby.url.ws;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.jetbrains.annotations.NotNull;

public record SeparatorFrameMetadata(byte separator, int maxAcceptorIdSize) implements FrameMetadata {
    @Override
    public void parse(@NotNull ByteBuf content, @NotNull Consumer consumer) {
        int index = content.indexOf(0, maxAcceptorIdSize, separator);
        if (index >= 0 && index + 10 <= content.readableBytes()) {
            ByteBuf id = content.readBytes(index);
            content.readBytes(1);  // separator
            long requestId = content.readLong();
            content.readBytes(1);  // separator
            consumer.accept(id, requestId, content);
        } else {
            consumer.accept(null, -1, content);
        }
    }

    @Override
    public @NotNull ByteBuf compose(long requestId, int code, byte @NotNull [] content) {
        ByteBuf result = Unpooled.buffer(content.length + 11);
        result.writeLong(requestId);
        result.writeByte(separator);
        result.writeByte(code);
        result.writeByte(separator);
        result.writeBytes(content);
        return result;
    }
}
