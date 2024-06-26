package io.spbx.webby.ws.impl;

import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.spbx.webby.netty.ws.FrameConst.StatusCodes;
import io.spbx.webby.netty.ws.sender.Sender;
import io.spbx.webby.ws.context.BaseRequestContext;
import io.spbx.webby.ws.context.ClientInfo;
import io.spbx.webby.ws.context.RequestContext;
import io.spbx.webby.ws.convert.FrameConverter;
import io.spbx.webby.ws.lifecycle.AgentLifecycle;
import io.spbx.webby.ws.lifecycle.AgentLifecycleFanOut;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record FrameConverterEndpoint(@NotNull Object agent,
                                     @NotNull FrameConverter<Object> converter,
                                     @Nullable Sender sender) implements AgentEndpoint {
    @Override
    public @NotNull AgentLifecycle lifecycle() {
        return AgentLifecycleFanOut.of(agent, sender, converter);
    }

    @Override
    public void processIncoming(@NotNull WebSocketFrame frame, @NotNull ClientInfo client, @NotNull CallResultConsumer consumer) {
        converter.toMessage(frame, (acceptor, payload, requestContext) -> {
            boolean forceRenderAsString = converter().peekFrameType(requestContext) == Boolean.TRUE;
            Object callResult = acceptor.call(agent, payload, requestContext, forceRenderAsString);
            consumer.accept(callResult, requestContext);
        });
    }

    @Override
    public @NotNull WebSocketFrame processOutgoing(@NotNull Object message, @NotNull RequestContext context) {
        return converter.toFrame(StatusCodes.OK, message, context);
    }

    @Override
    public @NotNull WebSocketFrame processError(int code, @NotNull String message, @NotNull BaseRequestContext context) {
        return converter.toFrame(code, message, context);
    }
}
