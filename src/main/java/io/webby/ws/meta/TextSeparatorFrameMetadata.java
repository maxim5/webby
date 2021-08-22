package io.webby.ws.meta;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.webby.netty.ws.Constants.RequestIds;
import io.webby.util.netty.EasyByteBuf;
import org.jetbrains.annotations.NotNull;

public record TextSeparatorFrameMetadata(byte separator, int maxAcceptorIdSize) implements FrameMetadata {
    public static final byte DEFAULT_SEPARATOR = (byte) ' ';
    private static final int MAX_LONG_LENGTH = 20;  // 9223372036854775807

    public TextSeparatorFrameMetadata() {
        this(DEFAULT_SEPARATOR, MAX_ID_SIZE);
    }

    public TextSeparatorFrameMetadata {
        assert maxAcceptorIdSize <= MAX_ID_SIZE :
                "The acceptorId size can't be larger than %d: %d".formatted(MAX_ID_SIZE, maxAcceptorIdSize);
    }

    @Override
    public void parse(@NotNull ByteBuf content, @NotNull MetadataConsumer consumer) {
        content.markReaderIndex();
        ByteBuf acceptorId = EasyByteBuf.readUntil(content, separator, 1, maxAcceptorIdSize);
        ByteBuf requestId = EasyByteBuf.readUntil(content, separator, 1, MAX_LONG_LENGTH);
        if (acceptorId == null || requestId == null) {
            consumer.accept(null, RequestIds.NO_ID, content.resetReaderIndex());
        } else {
            consumer.accept(acceptorId, EasyByteBuf.parseLongSafe(requestId, RequestIds.NO_ID), content);
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
