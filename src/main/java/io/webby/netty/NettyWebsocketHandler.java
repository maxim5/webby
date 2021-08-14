package io.webby.netty;

import com.google.common.flogger.FluentLogger;
import com.google.inject.Inject;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.util.ReferenceCountUtil;
import io.webby.netty.ws.FrameMapper;
import io.webby.url.ws.AgentEndpoint;
import io.webby.url.ws.lifecycle.AgentLifecycle;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

public class NettyWebsocketHandler extends ChannelInboundHandlerAdapter {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    private final AgentEndpoint endpoint;
    private final AgentLifecycle lifecycle;

    @Inject private FrameMapper mapper;

    public NettyWebsocketHandler(@NotNull AgentEndpoint endpoint) {
        this.endpoint = endpoint;
        this.lifecycle = endpoint.lifecycle();
    }

    @Override
    public void userEventTriggered(@NotNull ChannelHandlerContext context, @NotNull Object event) throws Exception {
        if (event instanceof WebSocketServerProtocolHandler.HandshakeComplete handshakeComplete) {
            log.at(Level.INFO).log("Netty websocket handler joined %s: %s", context, handshakeComplete.requestUri());
            lifecycle.onChannelConnected(context.channel());
        } else {
            super.userEventTriggered(context, event);
        }
    }

    @Override
    public void channelActive(@NotNull ChannelHandlerContext context) {
        log.at(Level.FINER).log("Websocket Channel is active: %s", context);
        lifecycle.onChannelRestored();
    }

    @Override
    public void channelInactive(@NotNull ChannelHandlerContext context) {
        log.at(Level.FINER).log("Websocket Channel is inactive: %s", context);
        lifecycle.onChannelClose();
    }

    @Override
    public void channelRead(@NotNull ChannelHandlerContext context, @NotNull Object message) {
        if (message instanceof WebSocketFrame frame) {
            try {
                handle(frame, context);
            } catch (Throwable throwable) {
                log.at(Level.SEVERE).withCause(throwable).log("Unexpected failure: %s", throwable.getMessage());
            } finally {
                ReferenceCountUtil.release(frame);
            }
        } else {
            context.fireChannelRead(message);
        }
    }

    private void handle(@NotNull WebSocketFrame frame, @NotNull ChannelHandlerContext context) {
        endpoint.processIncoming(frame, (requestId, callResult) -> {
            if (callResult == null) {
                log.at(Level.INFO).log("Websocket agent %s doesn't handle the frame: %s", endpoint.instance(), frame.getClass());
                return;
            }

            WebSocketFrame outgoing = endpoint.processOutgoing(requestId, callResult);
            if (outgoing == null) {
                outgoing = mapper.mapInstance(callResult);
            }
            if (outgoing != null) {
                context.channel().writeAndFlush(outgoing);
            } else {
                log.at(Level.WARNING).log("Websocket agent returned unexpected object: %s", callResult);
            }
        });
    }
}
