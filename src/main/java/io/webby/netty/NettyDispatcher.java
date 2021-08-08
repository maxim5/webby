package io.webby.netty;

import com.google.common.flogger.FluentLogger;
import com.google.inject.Inject;
import com.google.inject.Provider;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.webby.url.impl.UrlRouter;
import io.webby.url.ws.AgentEndpoint;

import java.util.logging.Level;

public class NettyDispatcher extends ChannelInboundHandlerAdapter {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    @Inject private UrlRouter router;
    @Inject private Provider<NettyRequestHandler> nettyChannelHandler;

    private ChannelPipeline pipeline;

    @Override
    public void handlerAdded(ChannelHandlerContext context) {
        pipeline = context.pipeline();
    }

    @Override
    public void channelRead(ChannelHandlerContext context, Object message) {
        log.at(Level.FINER).log("Dispatching %s\n", message);

        if (message instanceof HttpRequest request) {
            String uri = request.uri();
            AgentEndpoint endpoint = router.routeWebSocket(uri);

            if (endpoint != null) {
                pipeline.addLast(new WebSocketServerCompressionHandler());
                pipeline.addLast(new WebSocketServerProtocolHandler(uri, null, true));
                pipeline.addLast(new NettyWebsocketHandler(endpoint));
                pipeline.remove(ChunkedWriteHandler.class);
                pipeline.remove(NettyDispatcher.class);
            } else {
                pipeline.addLast(nettyChannelHandler.get());
                pipeline.remove(NettyDispatcher.class);
            }

            if (request instanceof FullHttpRequest full) {
                context.fireChannelRead(full.retain());
            }
            return;
        }

        if (message instanceof WebSocketFrame frame) {
            context.fireChannelRead(frame.retain());
            return;
        }

        log.at(Level.WARNING).log("Unknown message received: %s", message);
    }
}