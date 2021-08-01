package io.webby;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;

import java.util.Queue;
import java.util.stream.Stream;

public class CompositeHttpResponse extends DefaultHttpResponse implements ByteBufHolder {
    private ByteBuf content = Unpooled.EMPTY_BUFFER;

    protected CompositeHttpResponse(HttpVersion version, HttpResponseStatus status, HttpHeaders headers) {
        super(version, status, headers);
    }

    public static @NotNull CompositeHttpResponse fromObjects(@NotNull Queue<HttpObject> objects) {
        if (objects.isEmpty()) {
            Assertions.fail();
        }

        HttpResponse response = (HttpResponse) objects.poll();
        Assertions.assertFalse(response instanceof FullHttpResponse);
        CompositeHttpResponse httpResponse = new CompositeHttpResponse(response.protocolVersion(), response.status(), response.headers());

        Stream<HttpContent> stream = objects.stream().map(object -> (HttpContent) object);
        byte[] bytes = AssertResponse.readAll(stream);
        httpResponse.content = Unpooled.wrappedBuffer(bytes);

        return httpResponse;
    }

    public ByteBuf content() {
        return content;
    }

    @Override
    public ByteBufHolder copy() {
        return replace(content.copy());
    }

    @Override
    public ByteBufHolder duplicate() {
        return replace(content.duplicate());
    }

    @Override
    public ByteBufHolder retainedDuplicate() {
        return replace(content.retainedDuplicate());
    }

    @Override
    public ByteBufHolder replace(ByteBuf content) {
        return null;
    }

    @Override
    public int refCnt() {
        return content.refCnt();
    }

    @Override
    public ByteBufHolder retain() {
        content.retain();
        return this;
    }

    @Override
    public ByteBufHolder retain(int increment) {
        content.retain(increment);
        return this;
    }

    @Override
    public ByteBufHolder touch() {
        content.touch();
        return this;
    }

    @Override
    public ByteBufHolder touch(Object hint) {
        content.touch(hint);
        return this;
    }

    @Override
    public boolean release() {
        return content.release();
    }

    @Override
    public boolean release(int decrement) {
        return content.release(decrement);
    }
}
