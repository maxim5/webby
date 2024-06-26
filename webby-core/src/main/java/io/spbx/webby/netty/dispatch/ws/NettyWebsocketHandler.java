package io.spbx.webby.netty.dispatch.ws;

import com.google.common.flogger.FluentLogger;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.util.ReferenceCountUtil;
import io.spbx.webby.db.sql.ThreadLocalConnector;
import io.spbx.webby.netty.ws.FrameMapper;
import io.spbx.webby.netty.ws.errors.WebsocketError;
import io.spbx.webby.ws.context.ClientInfo;
import io.spbx.webby.ws.context.ErrorRequestContext;
import io.spbx.webby.ws.impl.AgentEndpoint;
import io.spbx.webby.ws.lifecycle.AgentLifecycle;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

public class NettyWebsocketHandler extends ChannelInboundHandlerAdapter {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    private final AgentEndpoint endpoint;
    private final ClientInfo clientInfo;
    private final AgentLifecycle lifecycle;

    @Inject private FrameMapper mapper;

    @Inject
    public NettyWebsocketHandler(@Assisted @NotNull AgentEndpoint endpoint, @Assisted @NotNull ClientInfo clientInfo) {
        this.endpoint = endpoint;
        this.clientInfo = clientInfo;
        this.lifecycle = endpoint.lifecycle();
        this.lifecycle.onBeforeHandshake(clientInfo);
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
                cleanupWorkingThread();
            }
        } else {
            context.fireChannelRead(message);
        }
    }

    @Override
    public void exceptionCaught(@NotNull ChannelHandlerContext context, @NotNull Throwable cause) {
        WebSocketFrame errorFrame = null;

        if (cause instanceof WebsocketError websocketError) {
            String replyError = websocketError.getReplyError();
            log.at(Level.WARNING).withCause(cause).log("Websocket error: %s", replyError);
            if (replyError != null) {
                errorFrame = endpoint.processError(websocketError.getCode(), replyError, ErrorRequestContext.DEFAULT);
            }
        } else {
            log.at(Level.SEVERE).withCause(cause).log("Unexpected failure: %s", cause.getMessage());
        }

        if (errorFrame != null) {
            context.channel().writeAndFlush(errorFrame);
        }
    }

    private void handle(@NotNull WebSocketFrame frame, @NotNull ChannelHandlerContext context) {
        endpoint.processIncoming(frame, clientInfo, (callResult, requestContext) -> {
            if (callResult == null) {
                return;
            }

            WebSocketFrame outgoing = null;
            try {
                outgoing = endpoint.processOutgoing(callResult, requestContext);
                if (outgoing == null) {
                    outgoing = mapper.mapInstance(callResult);
                    if (outgoing == null) {
                        log.at(Level.WARNING).log("Websocket agent returned unexpected object: %s", callResult);
                    }
                }
            } catch (WebsocketError e) {
                String replyError = e.getReplyError();
                log.at(Level.WARNING).withCause(e).log("Websocket error: %s", replyError);
                if (replyError != null) {
                    outgoing = endpoint.processError(e.getCode(), replyError, requestContext);
                }
            }

            if (outgoing != null) {
                context.channel().writeAndFlush(outgoing);
            }
        });
    }

    private static void cleanupWorkingThread() {
        ThreadLocalConnector.cleanupIfNecessary();
    }

    public interface Factory {
        @NotNull NettyWebsocketHandler create(@NotNull AgentEndpoint endpoint, @NotNull ClientInfo clientInfo);
    }
}
