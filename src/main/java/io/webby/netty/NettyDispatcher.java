package io.webby.netty;

import com.google.common.flogger.FluentLogger;
import com.google.inject.Inject;
import com.google.inject.Provider;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.webby.auth.session.Session;
import io.webby.auth.session.SessionManager;
import io.webby.auth.user.User;
import io.webby.auth.user.UserManager;
import io.webby.common.InjectorHelper;
import io.webby.netty.errors.ServeException;
import io.webby.netty.request.QueryParams;
import io.webby.netty.response.HttpResponseFactory;
import io.webby.netty.ws.errors.ClientDeniedException;
import io.webby.util.collect.Pair;
import io.webby.ws.context.ClientFrameType;
import io.webby.ws.context.ClientInfo;
import io.webby.ws.impl.AgentEndpoint;
import io.webby.ws.impl.WebsocketRouter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class NettyDispatcher extends ChannelInboundHandlerAdapter {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    public static final String PROTOCOL = "io.webby";

    @Inject private InjectorHelper helper;
    @Inject private WebsocketRouter websocketRouter;
    @Inject private Provider<NettyHttpHandler> httpHandler;
    @Inject private HttpResponseFactory factory;
    @Inject private SessionManager sessionManager;
    @Inject private UserManager userManager;

    private ChannelPipeline pipeline;

    @Override
    public void handlerAdded(@NotNull ChannelHandlerContext context) {
        pipeline = context.pipeline();
    }

    @Override
    public void channelRead(@NotNull ChannelHandlerContext context, @NotNull Object message) {
        log.at(Level.FINER).log("Dispatching %s\n", message);

        if (message instanceof HttpRequest request) {
            String uri = request.uri();
            AgentEndpoint endpoint = websocketRouter.route(uri);

            if (endpoint != null) {
                log.at(Level.FINER).log("Upgrading channel to Websocket: %s", uri);
                ClientInfo clientInfo = getClientInfo(request);
                pipeline.addLast(new WebSocketServerCompressionHandler());
                pipeline.addLast(new WebSocketServerProtocolHandler(uri, PROTOCOL, true));
                pipeline.addLast(helper.injectMembers(new NettyWebsocketHandler(endpoint, clientInfo)));
                pipeline.remove(ChunkedWriteHandler.class);
            } else {
                log.at(Level.FINER).log("Migrating channel to HTTP: %s", uri);
                pipeline.addLast(httpHandler.get());
            }

            pipeline.remove(NettyDispatcher.class);

            log.at(Level.FINER).log("New channel pipeline: %s", pipeline.names());
        } else if (message instanceof WebSocketFrame frame) {
            log.at(Level.INFO).log("Unexpected websocket frame: %s\n", frame);
        } else {
            log.at(Level.WARNING).log("Unknown message received: %s", message);
        }

        context.fireChannelRead(message);
    }

    @Override
    public void exceptionCaught(@NotNull ChannelHandlerContext context, @NotNull Throwable cause) {
        HttpResponse response = null;

        if (cause instanceof ClientDeniedException clientDeniedException) {
            String replyError = clientDeniedException.getReplyError();
            log.at(Level.WARNING).withCause(cause).log("Client denied: %s", replyError);
            if (replyError != null) {
                response = factory.newErrorResponse(HttpResponseStatus.BAD_REQUEST, replyError, cause);
            }
        } else if (cause instanceof ServeException serveException) {
            response = factory.handleServeException(serveException, "Dispatcher");
        } else {
            log.at(Level.SEVERE).withCause(cause).log("Unexpected failure: %s", cause.getMessage());
            response = factory.newResponse500("Unexpected failure", cause);
        }

        if (response != null) {
            context.channel().writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        }
    }

    // TODO: strict mode
    @VisibleForTesting
    @NotNull ClientInfo getClientInfo(@NotNull HttpRequest request) {
        QueryParams queryParams = QueryParams.fromDecoder(new QueryStringDecoder(request.uri()), Map.of());

        HttpHeaders headers = request.headers();
        String protocol = headers.get(HttpHeaderNames.SEC_WEBSOCKET_PROTOCOL);
        Map<String, String> protoParams = parseProtocol(protocol);

        String version = protoParams.getOrDefault("version", queryParams.getOrNull("v"));
        String type = protoParams.getOrDefault("type", queryParams.getOrNull("t"));
        String sessionId = protoParams.get("session");

        ClientFrameType preferredType = type == null ? null : switch (type) {
            case "text" -> ClientFrameType.TEXT;
            case "binary" -> ClientFrameType.BINARY;
            case "both" -> ClientFrameType.BOTH;
            case "any" -> ClientFrameType.ANY;
            default -> null;  // throw?
        };
        Session session = sessionManager.getSessionOrNull(sessionId);  // throw?
        User user = session != null ? userManager.findByUserId(session.userId()) : null;  // throw?

        return new ClientInfo(Optional.ofNullable(version),
                              Optional.ofNullable(preferredType),
                              Optional.ofNullable(session),
                              Optional.ofNullable(user));
    }

    @VisibleForTesting
    static @NotNull Map<String, String> parseProtocol(@Nullable String protocol) {
        if (protocol == null) {
            return Map.of();
        }
        return Arrays.stream(protocol.split(","))
                .filter(value -> value.indexOf('|') >= 0)
                .map(value -> Pair.of(value.trim().split("\\|", 2)))
                .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
    }
}
