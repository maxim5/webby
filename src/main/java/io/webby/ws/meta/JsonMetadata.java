package io.webby.ws.meta;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.webby.netty.marshal.JsonMarshaller;
import io.webby.netty.ws.Constants.RequestIds;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Map;

public record JsonMetadata(@NotNull JsonMarshaller json, @NotNull Charset charset) implements FrameMetadata {
    @Override
    public void parse(@NotNull ByteBuf content, @NotNull MetadataConsumer consumer) {
        Map<?, ?> map = json.readByteBuf(content, Map.class, charset);
        Object on = map.get("on");
        Object id = map.get("id");
        Object data = map.get("data");
        if (on instanceof String acceptorId && id instanceof Number requestId && data instanceof String inner) {
            consumer.accept(Unpooled.copiedBuffer(acceptorId, charset), requestId.longValue(), Unpooled.copiedBuffer(inner, charset));
        } else {
            consumer.accept(null, RequestIds.NO_ID, content);
        }
    }

    @Override
    public @NotNull ByteBuf compose(long requestId, int code, byte @NotNull [] content) {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>(3);
        map.put("id", requestId);
        map.put("code", code);
        map.put("data", new String(content, charset));
        return json.writeByteBuf(map, charset);
    }
}
