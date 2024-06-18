package io.spbx.webby.netty.dispatch.http;

import com.google.inject.Inject;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import org.jetbrains.annotations.NotNull;

class NettyHttpStack implements ChannelContextBound {
    @Inject private NettyHttpStepNavigator navigator;
    @Inject private NettyHttpStepCaller caller;
    @Inject private NettyHttpStepConverter converter;

    @Override
    public void bindContext(@NotNull ChannelHandlerContext context) {
        navigator.bindContext(context);
        caller.bindContext(context);
        converter.bindContext(context);
    }

    public @NotNull HttpResponse processIncoming(@NotNull FullHttpRequest incomingRequest) {
        // Step 1: navigate to endpoint
        return navigator.navigateToEndpoint(incomingRequest, (request, match, endpoint) -> {
            // Step 2: call endpoint
            return caller.callEndpoint(request, match, endpoint, (callResult, options) -> {
                // Step 3: convert to response
                return converter.convertToResponse(callResult, options);
            });
        });
    }
}
