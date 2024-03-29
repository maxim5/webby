package io.webby.testing;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import io.webby.netty.response.ContentHolder;
import org.jetbrains.annotations.NotNull;

import java.util.Queue;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class CompositeHttpResponse extends DefaultHttpResponse implements ContentHolder {
    private ByteBuf content = Unpooled.EMPTY_BUFFER;

    protected CompositeHttpResponse(HttpVersion version, HttpResponseStatus status, HttpHeaders headers) {
        super(version, status, headers);
    }

    public static @NotNull CompositeHttpResponse fromObjects(@NotNull Queue<HttpObject> objects) {
        assertFalse(objects.isEmpty(), "No objects provided: " + objects);

        HttpResponse response = (HttpResponse) objects.poll();
        assertFalse(response instanceof FullHttpResponse);
        CompositeHttpResponse httpResponse = new CompositeHttpResponse(response.protocolVersion(), response.status(), response.headers());

        Stream<HttpContent> stream = objects.stream().map(object -> (HttpContent) object);
        byte[] bytes = AssertResponse.readAllFrom(stream);
        httpResponse.content = Unpooled.wrappedBuffer(bytes);

        return httpResponse;
    }

    public ByteBuf content() {
        return content;
    }

    @Override
    public ByteBufHolder replace(ByteBuf content) {
        CompositeHttpResponse response = new CompositeHttpResponse(protocolVersion(), status(), headers());
        response.content = content;
        return response;
    }
}
