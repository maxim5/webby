package io.webby.netty;

import com.google.common.flogger.FluentLogger;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.util.ReferenceCountUtil;
import io.webby.url.ws.AgentEndpoint;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

public class NettyWebsocketHandler extends ChannelInboundHandlerAdapter {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    private final AgentEndpoint endpoint;

    public NettyWebsocketHandler(@NotNull AgentEndpoint endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public void userEventTriggered(@NotNull ChannelHandlerContext context, @NotNull Object event) throws Exception {
        if (event instanceof WebSocketServerProtocolHandler.HandshakeComplete handshakeComplete) {
            log.at(Level.INFO).log("Netty websocket handler joined %s: %s", context, handshakeComplete.requestUri());
        } else {
            super.userEventTriggered(context, event);
        }
    }

    @Override
    public void channelRead(@NotNull ChannelHandlerContext context, @NotNull Object message) throws Exception {
        if (message instanceof WebSocketFrame frame) {
            try {
                Object reply = endpoint.process(frame);
                if (reply != null) {
                    context.channel().writeAndFlush(reply);
                }
            } finally {
                ReferenceCountUtil.release(frame);
            }
        } else {
            context.fireChannelRead(message);
        }
    }
}
