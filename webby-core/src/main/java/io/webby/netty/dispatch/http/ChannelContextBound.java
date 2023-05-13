package io.webby.netty.dispatch.http;

import io.netty.channel.ChannelHandlerContext;
import org.jetbrains.annotations.NotNull;

interface ChannelContextBound {
    void bindContext(@NotNull ChannelHandlerContext context);
}
