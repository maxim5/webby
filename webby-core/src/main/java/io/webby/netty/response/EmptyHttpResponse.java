package io.webby.netty.response;

import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.*;

public class EmptyHttpResponse implements AsyncResponse, HttpResponse {
    public static final EmptyHttpResponse INSTANCE = new EmptyHttpResponse();

    @Override
    public HttpResponseStatus getStatus() {
        return status();
    }

    @Override
    public HttpResponseStatus status() {
        return HttpResponseStatus.ACCEPTED;
    }

    @Override
    public HttpResponse setStatus(HttpResponseStatus status) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public HttpVersion getProtocolVersion() {
        return protocolVersion();
    }

    @Override
    public HttpVersion protocolVersion() {
        return HttpVersion.HTTP_1_1;
    }

    @Override
    public HttpResponse setProtocolVersion(HttpVersion version) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public HttpHeaders headers() {
        return IgnoringHttpHeaders.INSTANCE;
    }

    @Override
    public DecoderResult getDecoderResult() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public DecoderResult decoderResult() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void setDecoderResult(DecoderResult result) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
