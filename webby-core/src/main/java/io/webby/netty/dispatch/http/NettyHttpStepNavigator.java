package io.webby.netty.dispatch.http;

import com.google.common.flogger.FluentLogger;
import com.google.inject.Inject;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.webby.routekit.Match;
import io.webby.routekit.Router;
import io.webby.util.base.CharArray;
import io.webby.util.base.MutableCharArray;
import io.webby.app.Settings;
import io.webby.netty.intercept.Interceptors;
import io.webby.netty.request.DefaultHttpRequestEx;
import io.webby.netty.request.HttpRequestFactory;
import io.webby.netty.response.HttpResponseFactory;
import io.webby.url.impl.Endpoint;
import io.webby.url.impl.RouteEndpoint;
import io.webby.util.func.TriFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.logging.Level;

class NettyHttpStepNavigator implements ChannelContextBound {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    @Inject private Settings settings;
    @Inject private Interceptors interceptors;
    @Inject private HttpRequestFactory requests;
    @Inject private HttpResponseFactory responses;
    @Inject private Router<RouteEndpoint> router;

    private ChannelHandlerContext context;

    @Override
    public void bindContext(@NotNull ChannelHandlerContext context) {
        assert this.context == null : "%s is not sharable: can't be added to multiple contexts".formatted(this);
        this.context = context;
    }

    @NotNull HttpResponse navigateToEndpoint(
            @NotNull FullHttpRequest request,
            @NotNull TriFunction<FullHttpRequest, Match<RouteEndpoint>, Endpoint, HttpResponse> next) {
        DecoderResult decoderResult = request.decoderResult();
        if (decoderResult.isFailure()) {
            log.at(Level.INFO).log("Failed to decode request: %s", decoderResult);
            return responses.newResponse400(decoderResult.cause());
        }

        CharArray path = extractPath(request.uri());
        Match<RouteEndpoint> match = router.routeOrNull(path);
        if (match == null) {
            log.at(Level.FINE).log("No associated endpoint for url: %s", path);
            return responses.newResponse404();
        }

        Endpoint endpoint = match.handler().getAcceptedEndpointOrNull(request);
        if (endpoint == null) {
            log.at(Level.INFO).log("Endpoint does not accept the request %s for url: %s", request.method(), path);
            return responses.newResponse404();
        }

        if (endpoint.context().bypassInterceptors()) {
            return next.apply(request, match, endpoint);
        } else {
            DefaultHttpRequestEx requestEx = requests.createRequest(request, context.channel(), endpoint.context());
            return interceptors.process(requestEx, endpoint, () -> next.apply(requestEx, match, endpoint));
        }
    }

    @VisibleForTesting
    @NotNull CharArray extractPath(@NotNull String uri) {
        MutableCharArray path = new MutableCharArray(uri);
        path.offsetEnd(path.length() - path.indexOfAny('?', '#', 0, path.length()));
        if (settings.getBoolProperty("netty.url.trailing.slash.ignore") && path.length() > 1) {
            path.offsetSuffix('/');
        }
        return path;
    }
}
