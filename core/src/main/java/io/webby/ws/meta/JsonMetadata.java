package io.webby.ws.meta;

import com.google.common.collect.ImmutableMap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.webby.netty.marshal.Json;
import io.webby.netty.ws.FrameConst.RequestIds;
import io.webby.netty.ws.errors.BadFrameException;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.Charset;
import java.util.Map;

public record JsonMetadata(@NotNull Json json, @NotNull Charset charset) implements FrameMetadata {
    @Override
    public void parse(@NotNull ByteBuf content, @NotNull MetadataConsumer consumer) {
        content.markReaderIndex();

        Map<?, ?> map;
        try {
            map = json.readByteBuf(content, Map.class);
        } catch (Throwable e) {
            throw new BadFrameException("Failed to parse json", e, null);
        }

        if (map.get("on") instanceof String acceptorId && acceptorId.length() > 0 &&
            map.get("id") instanceof Number requestId &&
            map.get("data") instanceof String inner) {
            consumer.accept(Unpooled.copiedBuffer(acceptorId, charset), requestId.longValue(), Unpooled.copiedBuffer(inner, charset));
        } else {
            consumer.accept(null, RequestIds.NO_ID, content.resetReaderIndex());
        }
    }

    @Override
    public @NotNull ByteBuf compose(long requestId, int code, byte @NotNull [] content) {
        Map<String, Object> map = ImmutableMap.of(
            "id", requestId,
            "code", code,
            "data", new String(content, charset)
        );
        return json.writeByteBuf(map);
    }
}
