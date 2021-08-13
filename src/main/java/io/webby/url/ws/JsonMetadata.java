package io.webby.url.ws;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.webby.netty.marshal.JsonMarshaller;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.Charset;
import java.util.Map;

public record JsonMetadata(@NotNull JsonMarshaller json, @NotNull Charset charset) implements FrameMetadata {
    @Override
    public void parse(@NotNull ByteBuf content, @NotNull Consumer consumer) {
        Map<?, ?> map = json.readByteBuf(content, Map.class, charset);
        Object on = map.get("on");
        Object id = map.get("id");
        Object data = map.get("data");
        if (on instanceof String acceptorId && id instanceof Long requestId) {
            consumer.accept(Unpooled.copiedBuffer(acceptorId, charset),
                            requestId,
                            Unpooled.copiedBuffer(data.toString(), charset));
        }
    }

    @Override
    public @NotNull ByteBuf compose(long requestId, int code, byte @NotNull [] content) {
        Map<String, Object> map = Map.of("id", requestId, "code", code, "data", new String(content, charset));
        return json.writeByteBuf(map, charset);
    }
}
