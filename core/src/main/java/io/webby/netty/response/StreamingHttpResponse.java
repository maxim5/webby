package io.webby.netty.response;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.*;
import io.netty.handler.stream.ChunkedInput;
import org.jetbrains.annotations.NotNull;

public class StreamingHttpResponse extends DefaultHttpResponse implements AsyncResponse {
    private final ChunkedInput<ByteBuf> input;

    public StreamingHttpResponse(HttpVersion version, HttpResponseStatus status, ChunkedInput<ByteBuf> input) {
        super(version, status);
        this.input = input;
    }

    public StreamingHttpResponse(HttpVersion version, HttpResponseStatus status, HttpHeaders headers, ChunkedInput<ByteBuf> input) {
        super(version, status, headers);
        this.input = input;
    }

    public @NotNull HttpChunkedInput chunkedContent() {
        return new HttpChunkedInput(input);
    }
}
