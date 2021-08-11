package io.webby.netty;

import com.google.common.flogger.FluentLogger;
import com.google.inject.Inject;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.util.ReferenceCountUtil;
import io.webby.url.ws.AgentLifecycle;
import io.webby.netty.ws.AgentLifecycleAdapter;
import io.webby.netty.ws.FrameMapper;
import io.webby.url.ws.AgentEndpoint;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

public class NettyWebsocketHandler extends ChannelInboundHandlerAdapter {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    private final AgentEndpoint endpoint;
    private final AgentLifecycle lifecycle;

    @Inject private FrameMapper mapper;

    public NettyWebsocketHandler(@NotNull AgentEndpoint endpoint) {
        this.endpoint = endpoint;
        this.lifecycle = endpoint.instance() instanceof AgentLifecycle lifecycle
                ? lifecycle
                : endpoint.sender() instanceof AgentLifecycle lifecycle ? lifecycle : new AgentLifecycleAdapter();
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
    public void channelRead(@NotNull ChannelHandlerContext context, @NotNull Object message) throws Exception {
        if (message instanceof WebSocketFrame frame) {
            try {
                handle(frame, context);
            } finally {
                ReferenceCountUtil.release(frame);
            }
        } else {
            context.fireChannelRead(message);
        }
    }

    private void handle(@NotNull WebSocketFrame message, @NotNull ChannelHandlerContext context) throws Exception {
        Object callResult = endpoint.process(message);
        if (callResult == null) {
            return;
        }

        WebSocketFrame frame = mapper.mapInstance(callResult);
        if (frame != null) {
            context.channel().writeAndFlush(frame);
        } else {
            log.at(Level.WARNING).log("Websocket agent returned unexpected object: %s", callResult);
        }
    }
}
