package io.spbx.webby.netty.response;

import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

import static io.spbx.util.base.EasyExceptions.notImplemented;

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
        throw notImplemented("EmptyHttpResponse.setStatus()");
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
        throw notImplemented("EmptyHttpResponse.setProtocolVersion()");
    }

    @Override
    public HttpHeaders headers() {
        return IgnoringHttpHeaders.INSTANCE;
    }

    @Override
    public DecoderResult getDecoderResult() {
        throw notImplemented("EmptyHttpResponse.getDecoderResult()");
    }

    @Override
    public DecoderResult decoderResult() {
        throw notImplemented("EmptyHttpResponse.decoderResult()");
    }

    @Override
    public void setDecoderResult(DecoderResult result) {
        throw notImplemented("EmptyHttpResponse.setDecoderResult()");
    }
}
