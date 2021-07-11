package io.webby.url.impl;

import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.Charset;

public class SimpleContentProvider implements ContentProvider {
    @Override
    public Object getContent(@NotNull ByteBuf byteBuf, @NotNull Charset charset) {
        return byteBuf.toString(charset);
    }
}
