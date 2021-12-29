package io.webby.netty.ws.sender;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.inject.ImplementedBy;
import io.netty.channel.ChannelFuture;
import io.webby.netty.ws.FrameConst;
import io.webby.ws.context.BaseRequestContext;
import io.webby.ws.context.EmptyContext;
import io.webby.ws.convert.OutFrameConverterListener;
import org.jetbrains.annotations.NotNull;

@ImplementedBy(ChannelMessageSender.class)
public interface MessageSender<M> extends Sender, OutFrameConverterListener<M> {
    @CanIgnoreReturnValue
    default @NotNull ChannelFuture sendMessage(@NotNull M message) {
        return sendMessage(FrameConst.StatusCodes.OK, message);
    }

    @CanIgnoreReturnValue
    default @NotNull ChannelFuture sendMessage(int code, @NotNull M message) {
        return sendMessage(code, message, EmptyContext.EMPTY_TEXT_CONTEXT);
    }

    @CanIgnoreReturnValue
    default @NotNull ChannelFuture sendMessage(@NotNull M message, @NotNull BaseRequestContext context) {
        return sendMessage(FrameConst.StatusCodes.OK, message, context);
    }

    @CanIgnoreReturnValue
    @NotNull ChannelFuture sendMessage(int code, @NotNull M message, @NotNull BaseRequestContext context);

    @CanIgnoreReturnValue
    default @NotNull ChannelFuture sendFlushMessage(@NotNull M message) {
        return sendFlushMessage(FrameConst.StatusCodes.OK, message);
    }

    @CanIgnoreReturnValue
    default @NotNull ChannelFuture sendFlushMessage(int code, @NotNull M message) {
        return sendMessage(code, message, EmptyContext.EMPTY_TEXT_CONTEXT);
    }

    @CanIgnoreReturnValue
    default @NotNull ChannelFuture sendFlushMessage(@NotNull M message, @NotNull BaseRequestContext context) {
        return sendFlushMessage(FrameConst.StatusCodes.OK, message, context);
    }

    @CanIgnoreReturnValue
    @NotNull ChannelFuture sendFlushMessage(int code, @NotNull M message, @NotNull BaseRequestContext context);
}
