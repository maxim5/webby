package io.spbx.webby.testing;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import io.spbx.util.testing.MoreTruth;
import io.spbx.webby.netty.response.ContentHolder;
import org.jetbrains.annotations.NotNull;

import java.util.Queue;
import java.util.stream.Stream;

import static com.google.common.truth.Truth.assertThat;

public class CompositeHttpResponse extends DefaultHttpResponse implements ContentHolder {
    private ByteBuf content = Unpooled.EMPTY_BUFFER;

    protected CompositeHttpResponse(HttpVersion version, HttpResponseStatus status, HttpHeaders headers) {
        super(version, status, headers);
    }

    public static @NotNull CompositeHttpResponse fromObjects(@NotNull Queue<HttpObject> objects) {
        MoreTruth.assertThat(objects.isEmpty()).withMessage("No objects provided: %s", objects).isFalse();

        HttpResponse response = (HttpResponse) objects.poll();
        assertThat(response instanceof FullHttpResponse).isFalse();
        CompositeHttpResponse httpResponse =
            new CompositeHttpResponse(response.protocolVersion(), response.status(), response.headers());

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
