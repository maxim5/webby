package io.webby.netty.response;

import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.*;

public class EmptyHttpResponse implements AsyncResponse, HttpResponse {
    @Override
    public HttpResponseStatus getStatus() {
        throw new UnsupportedOperationException("Not implemented");
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
        return HttpVersion.HTTP_1_1;
    }

    @Override
    public HttpVersion protocolVersion() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public HttpResponse setProtocolVersion(HttpVersion version) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public HttpHeaders headers() {
        return new DefaultHttpHeaders();  // TODO: can avoid this?
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
