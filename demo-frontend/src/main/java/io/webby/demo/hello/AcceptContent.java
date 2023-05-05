package io.webby.demo.hello;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.webby.netty.request.HttpRequestEx;
import io.webby.url.annotate.POST;
import io.webby.url.annotate.Param;
import io.webby.url.annotate.Serve;
import io.webby.url.convert.IntConverter;

import java.util.HashMap;
import java.util.List;

@Serve
public class AcceptContent {
    @Param(var = "id")
    public static final IntConverter paramId = IntConverter.POSITIVE;

    @POST(url = "/int/{id}")
    public Object noContent(int id) {
        return new HashMap<>();
    }

    @POST(url = "/strint/{str}/{y}")
    public String contentObject(CharSequence str, int y, Object content) {
        return "Vars: str=%s y=%d content=<%s>".formatted(str, y, content.toString());
    }

    @POST(url = "/content/bytebuf")
    public String contentBytebuf(HttpRequestEx request, ByteBuf content) {
        return "len=%d".formatted(content.readableBytes());
    }

    @POST(url = "/upload/file")
    public String uploadFiles(HttpRequestEx request) {
        HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(request);
        try {
            List<String> bodyParts = decoder.getBodyHttpDatas().stream()
                .map(Object::toString)
                .map(s -> s.replace("\r\n", "\n"))
                .map(s -> s.replaceAll("RealFile:\\s[a-zA-Z0-9/\\\\_:-]+", "RealFile: <temp-path>"))
                .map(s -> s.replaceAll("DeleteAfter: (true|false)", "DeleteAfter: <delete-after>"))
                .toList();
            return String.join("\n\n", bodyParts);
        } finally {
            decoder.destroy();
        }
    }
}
