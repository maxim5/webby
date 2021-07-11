package io.webby.url.impl;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpUtil;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.Charset;

public interface ContentProvider {
    Object getContent(@NotNull ByteBuf byteBuf, @NotNull Charset charset);

    default Object getContent(@NotNull FullHttpRequest request) {
        return getContent(request.content(), HttpUtil.getCharset(request));
    }
}
