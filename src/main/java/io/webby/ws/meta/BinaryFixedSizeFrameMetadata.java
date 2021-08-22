package io.webby.ws.meta;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.webby.netty.ws.Constants.RequestIds;
import org.jetbrains.annotations.NotNull;

public record BinaryFixedSizeFrameMetadata(int size) implements FrameMetadata {
    public BinaryFixedSizeFrameMetadata {
        assert size <= MAX_ID_SIZE : "The acceptorId size can't be larger than %d: %d".formatted(MAX_ID_SIZE, size);
    }

    @Override
    public void parse(@NotNull ByteBuf content, @NotNull MetadataConsumer consumer) {
        if (content.readableBytes() >= size + 8) {
            ByteBuf acceptorId = content.readBytes(size);
            long requestId = content.readLong();
            consumer.accept(acceptorId, requestId, content);
        } else {
            consumer.accept(null, RequestIds.NO_ID, content);
        }
    }

    @Override
    public @NotNull ByteBuf compose(long requestId, int code, byte @NotNull [] content) {
        ByteBuf result = Unpooled.buffer(content.length + 9);
        result.writeLong(requestId);
        result.writeByte(code);
        result.writeBytes(content);
        return result;
    }
}
