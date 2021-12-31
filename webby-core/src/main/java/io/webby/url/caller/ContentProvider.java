package io.webby.url.caller;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpUtil;
import io.webby.url.convert.ConversionError;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.Charset;

public interface ContentProvider {
    @Nullable Object getContent(@NotNull ByteBuf byteBuf, @NotNull Charset charset) throws Exception;

    default @Nullable Object getContent(@NotNull FullHttpRequest request) {
        try {
            return getContent(request.content(), HttpUtil.getCharset(request));
        } catch (Throwable e) {
            throw new ConversionError("Failed to parse request content", e);
        }
    }
}
