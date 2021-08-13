package io.webby.url.ws;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.webby.netty.marshal.JsonMarshaller;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.Charset;
import java.util.Map;

public record JsonMetaReader(@NotNull JsonMarshaller json, @NotNull Charset charset) implements FrameMetaReader {
    @Override
    public void readMeta(@NotNull ByteBuf byteBuf, @NotNull Consumer consumer) {
        Map<?, ?> map = json.readByteBuf(byteBuf, Map.class, charset);
        Object on = map.get("on");
        Object id = map.get("id");
        if (on instanceof String acceptorId && id instanceof Long requestId) {
            consumer.accept(Unpooled.copiedBuffer(acceptorId, charset), requestId);
        }
    }
}
