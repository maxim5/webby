package io.webby.ws.meta;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.webby.netty.ws.Constants.RequestIds;
import org.jetbrains.annotations.NotNull;

public record BinarySeparatorFrameMetadata(byte separator, int maxAcceptorIdSize) implements FrameMetadata {
    public static final byte DEFAULT_SEPARATOR = (byte) ' ';

    public BinarySeparatorFrameMetadata() {
        this(DEFAULT_SEPARATOR, MAX_ID_SIZE);
    }

    public BinarySeparatorFrameMetadata {
        assert maxAcceptorIdSize <= MAX_ID_SIZE :
                "The acceptorId size can't be larger than %d: %d".formatted(MAX_ID_SIZE, maxAcceptorIdSize);
    }

    @Override
    public void parse(@NotNull ByteBuf content, @NotNull MetadataConsumer consumer) {
        int size = content.readableBytes();
        int index = content.indexOf(0, Math.min(maxAcceptorIdSize, size), separator);
        if (index > 0 && index + 10 <= size) {
            ByteBuf id = content.readBytes(index);
            content.readBytes(1);  // separator
            long requestId = content.readLong();
            content.readBytes(1);  // separator
            consumer.accept(id, requestId, content);
        } else {
            consumer.accept(null, RequestIds.NO_ID, content);
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
