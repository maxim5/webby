package io.webby.ws.meta;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.webby.netty.ws.Constants.RequestIds;
import org.jetbrains.annotations.NotNull;

public record TextSeparatorFrameMetadata(byte separator, int maxAcceptorIdSize) implements FrameMetadata {
    public static final byte DEFAULT_SEPARATOR = (byte) ' ';
    private static final int MAX_LONG_LENGTH = 20;  // 9223372036854775807

    public TextSeparatorFrameMetadata() {
        this(DEFAULT_SEPARATOR, MAX_ID_SIZE);
    }

    @Override
    public void parse(@NotNull ByteBuf content, @NotNull MetadataConsumer consumer) {
        ByteBuf acceptorId = EasyByteBuf.readUntil(content, separator, maxAcceptorIdSize);
        ByteBuf requestId = EasyByteBuf.readUntil(content, separator, MAX_LONG_LENGTH);
        if (acceptorId == null || requestId == null) {
            consumer.accept(null, RequestIds.NO_ID, content);
        } else {
            consumer.accept(acceptorId, EasyByteBuf.parseLongSafely(requestId, RequestIds.NO_ID), content);
        }
    }

    @Override
    public @NotNull ByteBuf compose(long requestId, int code, byte @NotNull [] content) {
        ByteBuf result = Unpooled.buffer(content.length + 11);
        EasyByteBuf.writeLongString(requestId, result);
        result.writeByte(separator);
        EasyByteBuf.writeIntString(code, result);
        result.writeByte(separator);
        result.writeBytes(content);
        return result;
    }
}
